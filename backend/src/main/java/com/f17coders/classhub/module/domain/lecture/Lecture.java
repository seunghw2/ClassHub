package com.f17coders.classhub.module.domain.lecture;

import com.f17coders.classhub.module.domain.BaseEntity;
import com.f17coders.classhub.module.domain.category.Category;
import com.f17coders.classhub.module.domain.lectureBuy.LectureBuy;
import com.f17coders.classhub.module.domain.lectureLike.LectureLike;
import com.f17coders.classhub.module.domain.lectureSummary.LectureSummary;
import com.f17coders.classhub.module.domain.lectureTag.LectureTag;
import com.f17coders.classhub.module.domain.review.Review;
import com.f17coders.classhub.module.domain.studyTag.StudyTag;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lecture extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "lecture_id")
	private int lectureId;

	@Column(length = 40)
	private String siteLectureId;

	@Column(length = 300)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String image;

	@Enumerated(EnumType.STRING)
	private Level level;

	@Enumerated(EnumType.STRING)
	private SiteType siteType;

	private Integer priceOriginal;
	private Integer priceSale;

	@Column(columnDefinition = "TEXT")
	private String summary;

	@Column(columnDefinition = "TEXT")
	private String descriptionSummary;

	@Column(length = 150)
	private String descriptionDetail;

	private Float siteReviewRating;
	private Integer siteReviewCount;
	private Integer siteStudentCount;
	private Float reviewSum;
	private Integer reviewCount;

	private Integer totalTime;

	@Column(columnDefinition = "JSON")
	private String curriculum;

	@Column(length = 300)
	private String instructor;

	@Column(columnDefinition = "TEXT")
	private String siteLink;

	@Column(columnDefinition = "TEXT")
	private String gptReview;

	// Lecture - Category 연관 관계
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private Category category;

	// Lecture - tag 연관 관계, Lecture 있는 tag list
	@BatchSize(size = 16)	// 페이징에 필요한 만큼 Batch 처리
	@OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LectureTag> lectureTagList = new ArrayList<>();

	// Lecture - LectureLike 연관 관계
    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LectureLike> lectureLikeSet = new HashSet<>();

	// Lecture - review 연관 관계
    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewList = new ArrayList<>();

    // Lecture - LectureBuy 연관 관계
    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LectureBuy> lectureBuySet = new HashSet<>();
}

