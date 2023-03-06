package com.mentionall.cpr2u.education.service;

import com.mentionall.cpr2u.education.domain.*;
import com.mentionall.cpr2u.education.dto.EducationProgressDto;
import com.mentionall.cpr2u.education.dto.ScoreDto;
import com.mentionall.cpr2u.education.repository.EducationProgressRepository;
import com.mentionall.cpr2u.education.repository.LectureRepository;
import com.mentionall.cpr2u.user.domain.User;
import com.mentionall.cpr2u.user.repository.UserRepository;
import com.mentionall.cpr2u.util.exception.CustomException;
import com.mentionall.cpr2u.util.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationProgressService {
    private final UserRepository userRepository;
    private final EducationProgressRepository progressRepository;
    private final LectureRepository lectureRepository;

    public void completeQuiz(String userId, ScoreDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ResponseCode.NOT_FOUND_USER_EXCEPTION)
        );
        EducationProgress progress = progressRepository.findByUser(user).orElseThrow(
                () -> new CustomException(ResponseCode.EDUCATION_PROGRESS_NOT_FOUND)
        );

        // 이론 강의 수강 완료 후 퀴즈 테스트 가능
        if (progress.getLectureProgressStatus() != ProgressStatus.Completed)
            throw new CustomException(ResponseCode.EDUCATION_PROGRESS_BAD_REQUEST);

        progress.updateQuizScore(requestDto.getScore());

        if (requestDto.getScore() < TestStandard.quizScore)
            throw new CustomException(ResponseCode.EDUCATION_QUIZ_FAIL);
    }

    public void completePosture(String userId, ScoreDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ResponseCode.NOT_FOUND_USER_EXCEPTION)
        );
        EducationProgress progress = progressRepository.findByUser(user).orElseThrow(
                () -> new CustomException(ResponseCode.EDUCATION_PROGRESS_NOT_FOUND)
        );

        // 이론 강의 수강, 퀴즈 테스트 통과 후 자세 실습 테스트 가능
        if (progress.getLectureProgressStatus() != ProgressStatus.Completed ||
            progress.getQuizProgressStatus() != ProgressStatus.Completed)
            throw new CustomException(ResponseCode.EDUCATION_PROGRESS_BAD_REQUEST);

        progress.updatePostureScore(requestDto.getScore());

        if (progress.getPostureScore() < TestStandard.postureScore)
            throw new CustomException(ResponseCode.EDUCATION_POSTURE_FAIL);
    }

    public EducationProgressDto readEducationInfo(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ResponseCode.NOT_FOUND_USER_EXCEPTION)
        );
        EducationProgress progress = progressRepository.findByUser(user).orElseThrow(
                () -> new CustomException(ResponseCode.EDUCATION_PROGRESS_NOT_FOUND)
        );

        return new EducationProgressDto(progress, user);
    }

    public void completeLecture(String userId, Long lectureId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ResponseCode.NOT_FOUND_USER_EXCEPTION)
        );
        EducationProgress progress = progressRepository.findByUser(user).orElseThrow(
                () -> new CustomException(ResponseCode.EDUCATION_PROGRESS_NOT_FOUND)
        );
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(
                () -> new CustomException(ResponseCode.LECTURE_NOT_FOUND)
        );

        progress.updateLecture(lecture);
    }

    public void completeAllLectureCourse(String userId) {
        List<Lecture> lectureList = lectureRepository.findAllByType(LectureType.THEORY);
        Collections.sort(lectureList);
        lectureList.forEach(lecture -> completeLecture(userId, lecture.getId()));
    }
}
