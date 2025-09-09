import { useState, useEffect } from 'react';
import '../styles/GuidedTour.css';

const GuidedTour = () => {
  const [currentStep, setCurrentStep] = useState(0);
  const [isVisible, setIsVisible] = useState(false);

  const steps = [
    {
      element: '.calendar',
      content:
        '달력에서 원하는 날짜를 선택하여 일기 쓰기를 시작하세요. 감정에 따라 날짜가 색상으로 구분됩니다.',
    },
    {
      element: '.emotion-selector',
      content: '오늘의 기분을 가장 잘 표현하는 감정을 선택하세요.',
    },
    {
      element: '.entry-content',
      content:
        '당신의 하루와 감정에 대해 자세히 작성하세요. 항목은 비공개이며 자동으로 저장됩니다.',
    },
  ];

  useEffect(() => {
    const tourShown = localStorage.getItem('tourShown');
    if (!tourShown) {
      setIsVisible(true);
    }
  }, []);

  const handleNext = () => {
    if (currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      handleClose();
    }
  };

  const handleClose = () => {
    setIsVisible(false);
    localStorage.setItem('tourShown', 'true');
  };

  if (!isVisible) return null;

  return (
    <div className="guided-tour-overlay">
      <div className="guided-tour-content">
        <p>{steps[currentStep].content}</p>
        <div className="guided-tour-actions">
          <button onClick={handleClose} className="skip-btn">
            스킵
          </button>
          <button onClick={handleNext} className="next-btn">
            {currentStep === steps.length - 1 ? 'Got it!' : 'Next'}
          </button>
        </div>
        <div className="tour-progress">
          {steps.map((_, index) => (
            <div
              key={index}
              className={`progress-dot ${
                index === currentStep ? 'active' : ''
              }`}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

export default GuidedTour;
