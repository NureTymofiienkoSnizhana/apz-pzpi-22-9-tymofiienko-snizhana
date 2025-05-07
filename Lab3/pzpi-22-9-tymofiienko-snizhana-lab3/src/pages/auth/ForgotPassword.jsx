import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { forgotPassword } from '../../services/authService';
import './Login.css';
import petHouseLogo from '../../assets/pet-house.png';

const ForgotPassword =() => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!email) {
      setError('Будь ласка, введіть електронну пошту');
      return;
    }

    setMessage('');
    setError('');
    setLoading(true);

    try {
        await forgotPassword(email);
        setMessage('Інструкції щодо скидання паролю надіслано на вашу електронну пошту.');
    } catch (err) {
        setError(err.message || 'Не вдалося скинути пароль. Перевірте вказану електронну пошту.');
    } finally {
        setLoading(false);
    }
  };
  
  const backToLogin = () => {
    navigate('/login');
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-split">
          {/* Ліва частина з логотипом */}
          <div className="login-left">
            <div className="logo-container">
              <img src={petHouseLogo} alt="Pet House Logo" className="logo" />
              <h1>PetHealth</h1>
            </div>
          </div>
          
          {/* Вертикальна лінія розділювач */}
          <div className="login-divider"></div>
          
          {/* Права частина з формою */}
          <div className="login-right">
            <h2>Відновлення паролю</h2>
            
            {message && <div className="success-message">{message}</div>}
            {error && <div className="error-message">{error}</div>}
            
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="email">Введіть вашу електронну пошту</label>
                <input
                  type="email"
                  id="email"
                  placeholder="example@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              
              <button 
                type="submit" 
                className="login-button" 
                disabled={loading}
              >
                {loading ? 'Обробка...' : 'Відновити пароль'}
              </button>
            </form>
            
            <div className="login-footer">
              <button 
                onClick={backToLogin} 
                className="forgot-password-button"
              >
                Повернутись до входу
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
