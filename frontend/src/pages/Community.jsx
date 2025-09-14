import React, { useState } from 'react';
import { dummyData } from '../data/community';
import '../styles/Community.css';

const Community = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 16;

  const filteredData = dummyData.filter(
    (item) =>
      item.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.author.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredData.slice(indexOfFirstItem, indexOfLastItem);

  const totalPages = Math.ceil(filteredData.length / itemsPerPage);

  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  return (
    <div className="community-container">
      <div className="community-header">
        <button className="write-diary-button">일기 쓰기</button>
        <div className="search-bar">
          <input
            type="text"
            placeholder="제목, 닉네임으로 검색"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button>검색</button>
        </div>
      </div>

      <div className="diary-grid">
        {currentItems.map((item) => (
          <div key={item.id} className="diary-card">
            <div className="diary-card-header">
              <span className="diary-author">{item.author}</span>
              <span className="diary-date">{item.date}</span>
            </div>
            <hr />
            <div className="diary-card-body">
              <h3 className="diary-title">{item.title}</h3>
              <p className="diary-content">{item.content}</p>
            </div>
            <div className="diary-card-footer">
              <span className="diary-views">조회수 {item.views}</span>
              {item.isPopular && <span className="popular-badge">인기</span>}
            </div>
          </div>
        ))}
      </div>

      <div className="pagination">
        {Array.from({ length: totalPages }, (_, i) => i + 1).map((number) => (
          <button
            key={number}
            onClick={() => paginate(number)}
            className={currentPage === number ? 'active' : ''}
          >
            {number}
          </button>
        ))}
      </div>
    </div>
  );
};

export default Community;
