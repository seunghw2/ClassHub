package com.f17coders.classhub.module.domain.lecture.service;

import com.f17coders.classhub.global.exception.BaseExceptionHandler;
import com.f17coders.classhub.global.exception.code.ErrorCode;
import com.f17coders.classhub.module.domain.category.dto.resource.CategoryRes;
import com.f17coders.classhub.module.domain.job.Job;
import com.f17coders.classhub.module.domain.job.dto.response.JobRes;
import com.f17coders.classhub.module.domain.lecture.Lecture;
import com.f17coders.classhub.module.domain.lecture.dto.response.*;
import com.f17coders.classhub.module.domain.lecture.repository.LectureRepository;
import com.f17coders.classhub.module.domain.lectureBuy.repository.LectureBuyRepository;
import com.f17coders.classhub.module.domain.lectureSummary.service.LectureSummaryService;
import com.f17coders.classhub.module.domain.member.repository.MemberRepository;
import com.f17coders.classhub.module.domain.memberTag.MemberTag;
import com.f17coders.classhub.module.domain.memberTag.repository.MemberTagRepository;
import com.f17coders.classhub.module.domain.tag.Tag;
import com.f17coders.classhub.module.domain.tag.dto.response.TagListRes;
import com.f17coders.classhub.module.domain.tag.dto.response.TagRes;
import com.f17coders.classhub.module.domain.tag.repository.TagRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final TagRepository tagRepository;
    private final MemberRepository memberRepository;
    private final LectureBuyRepository lectureBuyRepository;

    @Override
    public LectureReadRes readLecture(int lectureId) throws BaseExceptionHandler, IOException {
        LectureReadLectureLikeCountRes lectureReadLectureLikeCountRes = lectureRepository.findLectureByLectureId(
            lectureId);

        List<TagRes> tagList = tagRepository.findTagsByLectureIdFetchJoinLectureTag(lectureId);

        String summaryText = lectureReadLectureLikeCountRes.summary();
        List<String> summaryList;
        if (StringUtils.hasText(summaryText)) {
            String[] array = summaryText.split("\\|\\|");
            summaryList = Arrays.stream(array)
                .collect(Collectors.toList());
        } else {
            summaryList = new ArrayList<>();
        }

        return LectureReadRes.builder()
            .lectureId(lectureId)
            .lectureName(lectureReadLectureLikeCountRes.lectureName())
            .instructor(lectureReadLectureLikeCountRes.instructor())
            .image(lectureReadLectureLikeCountRes.image())
            .level(lectureReadLectureLikeCountRes.level())
            .siteType(lectureReadLectureLikeCountRes.siteType())
            .siteLink(lectureReadLectureLikeCountRes.siteLink())
            .priceOriginal(lectureReadLectureLikeCountRes.priceOriginal())
            .priceSale(lectureReadLectureLikeCountRes.priceSale())
            .totalTime(lectureReadLectureLikeCountRes.totalTime())
            .curriculum(lectureReadLectureLikeCountRes.curriculum()) // 수정필요
            .category(lectureReadLectureLikeCountRes.category())
            .tagList(tagList)
            .lectureLikeCount(lectureReadLectureLikeCountRes.lectureLikeCount())
            .combinedRating(lectureReadLectureLikeCountRes.combinedRating())
            .combinedRatingCount(lectureReadLectureLikeCountRes.combinedRatingCount())
            .reviewRating(lectureReadLectureLikeCountRes.reviewRating())
            .siteReviewRating(lectureReadLectureLikeCountRes.siteReviewRating())
            .siteReviewCount(lectureReadLectureLikeCountRes.siteReviewCount())
            .siteStudentCount(lectureReadLectureLikeCountRes.siteStudentCount())
            .gptReview(lectureReadLectureLikeCountRes.gptReview())
            .descriptionSummary(lectureReadLectureLikeCountRes.descriptionSummary())
            .summary(summaryList)
            .descriptionDetail(lectureReadLectureLikeCountRes.descriptionDetail())
            .build();

    }

    @Override
    public LectureListRes getLectureList(Integer categoryId, String tags, String keyword,
        String level, String site, String order, Pageable pageable)    // 강의 목록 조회_최적화
        throws BaseExceptionHandler {

        List<Lecture> lectureList = lectureRepository.getLectureList(categoryId,
            tags, keyword, level, site, order, pageable);

        List<LectureListDetailRes> lectureDtoList = lectureList.stream()
            .map(lecture -> LectureListDetailRes.builder()
                .lectureId(lecture.getLectureId())
                .lectureName(lecture.getName())
                .siteType(lecture.getSiteType())
                .instructor(lecture.getInstructor())
                .image(lecture.getImage())
                .level(lecture.getLevel())
                .combinedRating(0.1f)
                .combinedRatingCount(10)
                .priceOriginal(10)
                .priceSale(10)
                .descriptionSummary(lecture.getDescriptionSummary())
                .totalTime(lecture.getTotalTime())
                .category(CategoryRes.builder()
                    .categoryId(lecture.getCategory().getCategoryId())
                    .categoryName(lecture.getCategory().getCategoryName())
                    .build())
                .tagList(lecture.getLectureTagList().stream()
                    .map(lectureTag -> lectureTag.getTag())
                    .map(tag -> TagRes.builder()
                        .tagId(tag.getTagId())
                        .name(tag.getName())
                        .build())
                    .toList())
                .lectureLikeCount(10)
                .build())
            .toList();

        int totalPages = (int) Math.ceil(
            (double) lectureRepository.countLectureBySearchCond(categoryId, tags,
                keyword, level, site) / pageable.getPageSize());

        return LectureListRes
            .builder()
            .lectureList(lectureDtoList)
            .totalPages(totalPages)
            .build();
    }


    // member가 만약 관심있어하는 태그가 없다면(사실 그러면 사고) 그래도 예외처리 필요
    @Override
    public LectureListTagRes getTop5LecturesByTag(int tagId) {
        List<LectureListDetailLectureLikeCountRes> lectures = lectureRepository.findTop5LecturesWithTagId(
            tagId);

        Tag tag = tagRepository.findTagByTagId(tagId);

        return LectureListTagRes
            .builder()
            .lectureList(lectures)
            .tag(
                TagRes.builder()
                    .tagId(tag.getTagId())
                    .name(tag.getName()).build()
            )
            .build();
    }

    @Override
    public LectureListJobRes getLecturesByFamousJob() {
        List<Job> jobs = memberRepository.findRandomFamousJobIds()
            .orElseThrow(() -> new BaseExceptionHandler(ErrorCode.NOT_FOUND_ITEM_EXCEPTION));

        if (jobs.isEmpty()) {
            throw new BaseExceptionHandler(ErrorCode.NOT_FOUND_ITEM_EXCEPTION);
        }

        int randomIndex = new Random().nextInt(jobs.size());
        Job job = jobs.get(randomIndex);

        List<Integer> lectureIds = lectureBuyRepository.getLectureIdsByJobId(job.getJobId());
        if (lectureIds.size() != 5) {
            List<Integer> spareLectures = lectureRepository.getFamousLectureIds();
            lectureIds.addAll(spareLectures.subList(0, 5 - lectureIds.size()));
        }

        List<LectureListDetailLectureLikeCountRes> lectures = lectureRepository.findLecturesByLectureIds(
            lectureIds);

        return LectureListJobRes.builder()
            .lectureList(lectures)
            .job(
                JobRes.builder()
                    .jobId(job.getJobId())
                    .name(job.getName()).build()
            )
            .build();
    }

    @Override
    public LectureListJobRes getLecturesByDesiredJob(int memberId) {

        Job job = memberRepository.findJobIdByMemberId(memberId);
        if (job == null) {
            return getLecturesByFamousJob();
        }

        List<Integer> lectureIds = lectureBuyRepository.getLectureIdsByJobId(job.getJobId());
        if (lectureIds.size() != 5) {
            List<Integer> spareLectures = lectureRepository.getFamousLectureIds();
            lectureIds.addAll(spareLectures.subList(0, 5 - lectureIds.size()));
        }

        List<LectureListDetailLectureLikeCountRes> lectures = lectureRepository.findLecturesByLectureIds(
            lectureIds);

        return LectureListJobRes.builder()
            .lectureList(lectures)
            .job(
                JobRes.builder()
                    .jobId(job.getJobId())
                    .name(job.getName()).build()
            )
            .build();
    }


}
