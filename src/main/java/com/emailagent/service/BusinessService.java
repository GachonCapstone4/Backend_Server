package com.emailagent.service;

import com.emailagent.domain.entity.*;
import com.emailagent.dto.request.*;
import com.emailagent.dto.response.*;
import com.emailagent.exception.ResourceNotFoundException;
import com.emailagent.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessProfileRepository profileRepository;
    private final BusinessResourceRepository resourceRepository;
    private final BusinessFaqRepository faqRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    // =============================================
    // 비즈니스 프로필 (Upsert)
    // =============================================

    @Transactional(readOnly = true)
    public BusinessProfileResponse getProfile(Long userId) {
        return profileRepository.findByUser_UserId(userId)
                .map(BusinessProfileResponse::from)
                .orElse(null); // 프로필 없으면 null (최초 온보딩 전)
    }

    @Transactional
    public BusinessProfileResponse upsertProfile(Long userId, BusinessProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        BusinessProfile profile = profileRepository.findByUser_UserId(userId)
                .orElse(BusinessProfile.builder().user(user).build());

        profile.update(request.getIndustryType(), request.getEmailTone(), request.getCompanyDescription());
        return BusinessProfileResponse.from(profileRepository.save(profile));
    }

    // =============================================
    // 비즈니스 파일 (Resources)
    // =============================================

    @Transactional(readOnly = true)
    public List<BusinessResourceResponse> getFiles(Long userId) {
        return resourceRepository.findByUser_UserId(userId)
                .stream()
                .map(BusinessResourceResponse::from)
                .toList();
    }

    @Transactional
    public BusinessResourceResponse uploadFile(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 파일 저장 경로 생성
        String originalFileName = file.getOriginalFilename();
        String savedFileName = UUID.randomUUID() + "_" + originalFileName;
        Path uploadPath = Paths.get(uploadDir, String.valueOf(userId));
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 파일 타입 추출 (확장자 기준)
        String fileType = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileType = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toUpperCase();
        }

        BusinessResource resource = BusinessResource.builder()
                .user(user)
                .title(originalFileName)
                .fileName(originalFileName)
                .filePath(filePath.toString())
                .fileType(fileType)
                .build();

        return BusinessResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional
    public void deleteFile(Long userId, Long resourceId) {
        BusinessResource resource = resourceRepository
                .findByResourceIdAndUser_UserId(resourceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("파일을 찾을 수 없습니다."));

        // 실제 파일 삭제
        try {
            Files.deleteIfExists(Paths.get(resource.getFilePath()));
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", resource.getFilePath());
        }

        resourceRepository.delete(resource);
    }

    // =============================================
    // FAQ
    // =============================================

    @Transactional(readOnly = true)
    public List<FaqResponse> getFaqs(Long userId) {
        return faqRepository.findByUser_UserId(userId)
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    @Transactional
    public FaqResponse createFaq(Long userId, FaqRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        BusinessFaq faq = BusinessFaq.builder()
                .user(user)
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .build();

        return FaqResponse.from(faqRepository.save(faq));
    }

    @Transactional
    public FaqResponse updateFaq(Long userId, Long faqId, FaqRequest request) {
        BusinessFaq faq = faqRepository.findByFaqIdAndUser_UserId(faqId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ를 찾을 수 없습니다."));

        faq.update(request.getQuestion(), request.getAnswer());
        return FaqResponse.from(faq);
    }

    @Transactional
    public void deleteFaq(Long userId, Long faqId) {
        BusinessFaq faq = faqRepository.findByFaqIdAndUser_UserId(faqId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ를 찾을 수 없습니다."));
        faqRepository.delete(faq);
    }

    // =============================================
    // 카테고리
    // =============================================

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long userId) {
        return categoryRepository.findByUser_UserId(userId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if (categoryRepository.existsByUser_UserIdAndCategoryName(userId, request.getCategoryName())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다: " + request.getCategoryName());
        }

        Category category = Category.builder()
                .user(user)
                .categoryName(request.getCategoryName())
                .color(request.getColor())
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다."));
        categoryRepository.delete(category);
    }
}
