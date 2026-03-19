package com.studentlife.StudentLifeAPIs.Config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}


//@Service
//public class ImageService {
//    @Autowired
//    private Cloudinary cloudinary;
//
//    projectName is a folder name in cloudinary
//    public Map uploadImage(MultipartFile file, String projectName) throws IOException {
//        // This will upload to a folder named after your project
//        return cloudinary.uploader().upload(file.getBytes(),
//                ObjectUtils.asMap("folder", projectName));
//    }
//}
