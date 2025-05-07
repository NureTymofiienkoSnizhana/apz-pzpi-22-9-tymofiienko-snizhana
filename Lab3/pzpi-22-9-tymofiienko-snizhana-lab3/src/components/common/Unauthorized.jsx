import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './Unauthorized.css';
import petHouseLogo from '../../assets/pet-house.png';

const Unauthorized = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  const goBack = () => {
    const from = location.state?.from?.pathname || "/login";
    try {
      navigate(from);
    } catch (error) {
      console.error('Помилка навігації:', error);
      navigate('/login');
    }
  };
  
  return (
    <div className="unauthorized-container">
      <div className="unauthorized-card">
        <div className="logo-container">
          <img src={petHouseLogo} alt="Pet House Logo" className="logo" />
          <h1>PetHealth</h1>
        </div>
        <h2>Доступ заборонено</h2>
        <p>У вас немає дозволу на доступ до цієї сторінки.</p>
        
        <button 
          onClick={goBack}
          className="back-button"
        >
          Повернутися на логін
        </button>
      </div>
    </div>
  );
};

export default Unauthorized;