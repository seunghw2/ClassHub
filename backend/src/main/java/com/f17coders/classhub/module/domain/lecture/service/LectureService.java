package com.f17coders.classhub.module.domain.lecture.service;

import com.f17coders.classhub.global.exception.BaseExceptionHandler;
import com.f17coders.classhub.module.domain.lecture.dto.response.LectureListJobRes;
import com.f17coders.classhub.module.domain.lecture.dto.response.LectureListRes;
import com.f17coders.classhub.module.domain.lecture.dto.response.LectureListTagRes;
import com.f17coders.classhub.module.domain.lecture.dto.response.LectureReadRes;
import java.io.IOException;
import org.springframework.data.domain.Pageable;

public interface LectureService {

	LectureReadRes readLecture(int lectureId) throws BaseExceptionHandler, IOException;

	LectureListRes getLectureList(Integer categoryId, String tags, String keyword, String level,
		String site,
		String order, Pageable pageable) throws BaseExceptionHandler, IOException;

	LectureListTagRes getTop5LecturesByTag(int tagId);

	LectureListJobRes getLecturesByFamousJob();

	LectureListJobRes getLecturesByDesiredJob(int memberId);

}
