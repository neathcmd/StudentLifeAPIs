package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.StudyPlanResponse;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Service.StudyPlanService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.forbidden;
import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;
import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class StudyPlanServiceImpl implements StudyPlanService {

    private final AssignmentRepository assignmentRepository;
    private final AuthUtil authUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String groqApiKey;

//    :TODO put it in env after testing after it work
    private static final String GROQ_URL =
        "https://api.groq.com/openai/v1/chat/completions";

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public ApiResponse<StudyPlanResponse> generateStudyPlan(Long assignmentId) {
        Users currentUser = authUtil.getAuthenticatedUser();
        Assignments assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment not found."));

        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw forbidden("You do not have access to this resource.");
        }

        String dueDate = assignment.getDueDate().format(DISPLAY_FMT);
        String description = assignment.getDescription() != null ? assignment.getDescription().substring(0, Math.min(200, assignment.getDescription().length()))
                : "no description provided.";

        String basicPlan = buildBasicPlan(assignment);

        // Build the prompt
        String prompt = """
                Your task is to IMPROVE an existing study plan for a student assignment.
                Rules:
                - Keep the response SHORT and structured.
                - Do NOT add unnecessary explanations.
                - Do NOT repeat the input.
                - Output ONLY the improved plan as a bullet list.
                - Each bullet should be clear, actionable, and student-friendly.
                - Keep it realistic based on the available time.
 
                Context:
                Title: %s
                Subject: %s
                Due Date: %s
                Description: %s
                Existing Plan:
                %s
 
                Instructions:
                - Refine and improve the plan.
                - Make tasks more specific and useful.
                - Adjust wording to match the assignment context.
                - Do NOT increase the number of steps significantly.
                - Do NOT add extra sections.
 
                Output format example:
                - Day 1–2: ...
                - Day 3–4: ...
                - Day 5: ...
                """.formatted(
                assignment.getTitle(),
                assignment.getSubject(),
                dueDate,
                description,
                basicPlan
        );

        String plan = callGroq(prompt);

        return new ApiResponse<>(
                200,
                true,
                "Study plan generated successfully.",
                StudyPlanResponse.builder()
                        .assignmentId(assignmentId)
                        .plan(plan)
                        .build()
        );
    }

    private String buildBasicPlan(Assignments assignment) {
        long daysLeft = DAYS.between(
                LocalDateTime.now(), assignment.getDueDate()
        );
        daysLeft = Math.max(daysLeft, 1);

        if (daysLeft <= 2) {
            return "- Day 1: Research and gather materials\n- Day 2: Write and submit";
        } else if (daysLeft <= 5) {
            return "- Day 1-2: Research\n- Day 3-4: Write draft\n- Day 5: Review and submit";
        } else {
            return "- Day 1-2: Understand requirements\n- Day 3-4: Research\n- Day 5-6: Write draft\n- Day 7: Review\n- Final day: Submit";
        }
    }

    private String callGroq(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            Map<String, Object> body = Map.of(
                    "model", "llama-3.1-8b-instant",
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    }
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(GROQ_URL, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText("Could not generate plan. Please try again.");

        } catch (Exception e) {
            log.error("[StudyPlan] Groq API error: {}", e.getMessage());
            throw new RuntimeException("Failed to generate study plan. Please try again.");
        }
    }
}
