import '../styles/MonthSelector.css';

const MonthSelector = ({ isOpen, onClose, onSelect, currentYear }) => {
  if (!isOpen) return null;

  const months = [
    '1월',
    '2월',
    '3월',
    '4월',
    '5월',
    '6월',
    '7월',
    '8월',
    '9월',
    '10월',
    '11월',
    '12월',
  ];

  return (
    <div className="month-selector-overlay" onClick={onClose}>
      <div className="month-selector" onClick={(e) => e.stopPropagation()}>
        <h3>{currentYear}</h3>
        <div className="months-grid">
          {months.map((month, index) => (
            <button
              key={month}
              className="month-button"
              onClick={() => onSelect(index)}
            >
              {month}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default MonthSelector;
