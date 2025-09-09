import { useState } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';
import '../styles/Analysis.css';
import { BarChart2 } from 'lucide-react';

const EMOTION_COLORS = ['#bca7f7', '#a0a4ac', '#6fcf6f', '#ffe066'];
const EMOTION_LABELS = ['행복', '슬픔', '화남', '그럭저럭'];

const initialData = [
  { name: '행복', value: 10 },
  { name: '슬픔', value: 5 },
  { name: '화남', value: 2 },
  { name: '그럭저럭', value: 3 },
];

const depressionQuestions = [
  '최근 2주간 우울하거나 슬펐던 적이 있나요?',
  '의욕이 없거나 피곤함을 자주 느끼나요?',
  '잠이 잘 오지 않거나 너무 많이 자나요?',
  '식욕이 줄거나 늘었나요?',
  '자신감이 떨어진 적이 있나요?',
];
const anxietyQuestions = [
  '최근 2주간 불안하거나 초조함을 느꼈나요?',
  '걱정이 멈추지 않는다고 느끼나요?',
  '긴장되거나 안절부절 못한 적이 있나요?',
  '쉽게 깜짝 놀라거나 예민해졌나요?',
  '집중이 잘 안 된다고 느끼나요?',
];

const CheckForm = ({ questions, onSubmit, type }) => {
  const [answers, setAnswers] = useState(Array(5).fill(0));
  const handleChange = (idx, value) => {
    const arr = [...answers];
    arr[idx] = Number(value);
    setAnswers(arr);
  };
  return (
    <form
      className="check-form"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit(answers.reduce((a, b) => a + b, 0));
      }}
    >
      <h4>{type === 'depression' ? '우울 체크' : '불안 체크'}</h4>
      {questions.map((q, i) => (
        <div className="check-q" key={i}>
          <span>{q}</span>
          <select
            value={answers[i]}
            onChange={(e) => handleChange(i, e.target.value)}
          >
            <option value={0}>전혀 아니다</option>
            <option value={1}>가끔</option>
            <option value={2}>자주</option>
            <option value={3}>항상</option>
          </select>
        </div>
      ))}
      <button className="check-btn" type="submit">
        시작하기
      </button>
    </form>
  );
};

const CheckResult = ({ score, type, onReset }) => {
  let msg = '';
  if (score <= 3) msg = '정상 범위입니다!';
  else if (score <= 7) msg = '약간의 주의가 필요해요.';
  else msg = '전문가 상담을 권장합니다.';
  return (
    <div className="check-result">
      <h4>{type === 'depression' ? '우울 체크 결과' : '불안 체크 결과'}</h4>
      <div className="check-score">점수: {score}점</div>
      <div className="check-msg">{msg}</div>
      <button className="check-btn" onClick={onReset}>
        다시하기
      </button>
    </div>
  );
};

const Analysis = () => {
  const [depressionScore, setDepressionScore] = useState(null);
  const [anxietyScore, setAnxietyScore] = useState(null);

  // 현재 날짜를 "YYYY년 MM월 DD일" 형식으로 반환하는 동적인 함수
  const getCurrentDate = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    const day = now.getDate();
    return `${year}년 ${month}월 ${day}일`;
  };

  return (
    <div className="analysis-bg">
      <div className="analysis-title-block">
        <h2 className="analysis-title">{getCurrentDate()}</h2>
      </div>
      <div className="analysis-cards">
        <div className="analysis-report-card">
          <BarChart2 size={32} color="#bca7f7" />
          <div className="report-label">월간 리포트</div>
          <button className="report-btn">보러가기</button>
        </div>
        <div className="analysis-report-card">
          <BarChart2 size={32} color="#bca7f7" />
          <div className="report-label">연간 리포트</div>
          <button className="report-btn">보러가기</button>
        </div>
      </div>
      <div className="analysis-chart-card">
        <div className="chart-title">기분 분포</div>
        <ResponsiveContainer width="100%" height={240}>
          <PieChart>
            <Pie
              data={initialData}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              innerRadius={48}
              outerRadius={70}
              fill="#8884d8"
              label={({ name, percent }) =>
                `${name} ${(percent * 100).toFixed(0)}%`
              }
            >
              {initialData.map((entry, idx) => (
                <Cell
                  key={`cell-${idx}`}
                  fill={EMOTION_COLORS[idx % EMOTION_COLORS.length]}
                />
              ))}
            </Pie>
          </PieChart>
        </ResponsiveContainer>
      </div>
      <div className="analysis-check-row">
        <div className="analysis-check-card">
          {depressionScore === null ? (
            <CheckForm
              questions={depressionQuestions}
              onSubmit={setDepressionScore}
              type="depression"
            />
          ) : (
            <CheckResult
              score={depressionScore}
              type="depression"
              onReset={() => setDepressionScore(null)}
            />
          )}
        </div>
        <div className="analysis-check-card">
          {anxietyScore === null ? (
            <CheckForm
              questions={anxietyQuestions}
              onSubmit={setAnxietyScore}
              type="anxiety"
            />
          ) : (
            <CheckResult
              score={anxietyScore}
              type="anxiety"
              onReset={() => setAnxietyScore(null)}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default Analysis;
