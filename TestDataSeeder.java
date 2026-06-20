package com.calendar.service;

import com.calendar.model.ChecklistItem;
import com.calendar.model.DayData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TestDataSeeder {
    private static final String[][] CHECKLIST_POOL = {
            {"아침 운동 30분", "물 2L 마시기", "독서 30분", "영어 단어 20개"},
            {"스트레칭", "점심 도시락 준비", "업무 메일 확인", "저녁 산책"},
            {"빨래하기", "방 정리", "과제 제출", "일기 쓰기"},
            {"명상 10분", "책 1장 읽기", "친구에게 연락", "일찍 자기"},
            {"비타민 챙기기", "뉴스레터 정리", "코딩 연습 1시간", "물 2L 마시기"},
            {"가계부 기록", "운동", "장보기", "설거지"},
    };

    private static final String[] DIARY_SAMPLES = {
            "6월 시작! 새 달 목표를 세웠다. 아침에 일찍 일어나니 하루가 길게 느껴진다.",
            "오후에 비가 와서 카페에서 공부했다. 라떼 한 잔과 함께 집중이 잘 됐다.",
            "바쁜 하루였지만 저녁에 산책하면서 마음을 정리했다.",
            "체크리스트를 다 채우지 못했지만, 내일은 더 잘해보자.",
            "친구랑 저녁 먹으면서 오랜만에 수다 떨었다. 기분 좋은 금요일.",
            "주말이라 늦잠 잤지만, 오후엔 운동하고 책 읽었다.",
            "일요일 — 다음 주 계획을 캘린더에 적어두었다. 준비 완료.",
            "월요병... 그래도 점심시간에 잠깐 산책하니 나아졌다.",
            "프로젝트 마감이 다가와서 바빴다. 그래도 물 마시기는 지켰다!",
            "오늘은 집중력이 좋았다. 체크리스트 절반 이상 달성.",
            "비 오는 날, 창밖 보면서 차 마시며 여유를 즐겼다.",
            "운동 후 상쾌했다. 땀 흘리니 스트레스가 풀리는 느낌.",
            "일기 쓰는 습관이 생기기 시작했다. 작은 성취.",
            "오후 회의가 길어져서 계획이 어긋났다. 내일 보충하자.",
            "거의 다 못했지만, 실패한 날도 기록하는 게 중요하다고 생각한다.",
            "다시 리듬을 찾은 것 같다. 체크리스트 4개 중 3개 완료!",
            "어제보다 나아졌다. 꾸준함이 답인 것 같다.",
            "오늘 하루도 수고했다. 내일은 100% 달성을 목표로!",
    };

    private TestDataSeeder() {
    }

    /** 이번 달 1일 ~ 오늘까지 데모 데이터를 채웁니다. */
    public static void seedMonthToToday(DataStore store) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);

        int[][] completionPlans = {
                {4, 4}, {2, 3}, {1, 3}, {5, 5}, {2, 4}, {1, 4},
                {3, 4}, {0, 3}, {4, 4}, {3, 5}, {2, 5}, {6, 6},
                {1, 2}, {3, 6}, {0, 4}, {4, 5}, {5, 5}, {2, 4},
        };
        boolean[] hasDiary = {
                true, true, false, false, true, true, true, false,
                true, true, true, true, false, true, true, true, true, true,
        };

        int dayIndex = 0;
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            int planIdx = dayIndex % completionPlans.length;
            int done = completionPlans[planIdx][0];
            int total = completionPlans[planIdx][1];
            boolean diary = hasDiary[dayIndex % hasDiary.length];

            DayData data = buildDayData(dayIndex, done, total, diary);
            store.save(date, data);
            dayIndex++;
        }
    }

    private static DayData buildDayData(int dayIndex, int doneCount, int totalCount, boolean withDiary) {
        DayData data = new DayData();

        if (withDiary) {
            data.setDiary(DIARY_SAMPLES[dayIndex % DIARY_SAMPLES.length]);
        }

        String[] pool = CHECKLIST_POOL[dayIndex % CHECKLIST_POOL.length];
        List<ChecklistItem> items = new ArrayList<>();
        for (int i = 0; i < totalCount; i++) {
            String text = pool[i % pool.length];
            if (i > 0 && i >= pool.length) {
                text = pool[i % pool.length] + " (" + (i + 1) + ")";
            }
            items.add(new ChecklistItem(text, i < doneCount));
        }
        data.setChecklist(items);
        return data;
    }
}
