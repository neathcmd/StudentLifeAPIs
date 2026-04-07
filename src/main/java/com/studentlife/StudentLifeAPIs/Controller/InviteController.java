package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.Service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
public class InviteController {

    private final AssignmentService assignmentService;

    @GetMapping("/accept")
    public RedirectView accept(@RequestParam String token) {
        return assignmentService.processInviteToken(token, true);
    }

    @GetMapping("/decline")
    public RedirectView decline(@RequestParam String token) {
        return assignmentService.processInviteToken(token, false);
    }
}
