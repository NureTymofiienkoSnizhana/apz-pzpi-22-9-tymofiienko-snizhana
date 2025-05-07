import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';
import './Login.css';
import petHouseLogo from '../../assets/pet-house.png';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { setCurrentUser } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!email || !password) {
      setError('Будь ласка, введіть електронну пошту та пароль');
      return;
    }
    
    setError('');
    setLoading(true);

    try {
      const userData = await login(email, password);
      
      setCurrentUser(userData);
      
      if (userData.role === 'admin') {
        navigate('/admin/dashboard');
      } else if (userData.role === 'vet') {
        navigate('/vet/dashboard');
      }
    } catch (err) {
      console.error('Помилка входу:', err);
      setError(err.message || 'Невдала спроба входу. Перевірте ваші дані.');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = () => {
    navigate('/forgot-password');
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-split">
          <div className="login-left">
            <div className="logo-container">
              <img src={petHouseLogo} alt="Pet House Logo" className="logo" />
              <h1>PetHealth</h1>
            </div>
          </div>
          
          <div className="login-divider"></div>
          
          <div className="login-right">
            <h2>Увійти до системи</h2>
            
            {error && <div className="error-message">{error}</div>}
            
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="email">Електронна пошта</label>
                <input
                  type="email"
                  id="email"
                  placeholder="example@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="password">Пароль</label>
                <input
                  type="password"
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
              
              <button 
                type="submit" 
                className="login-button" 
                disabled={loading}
              >
                {loading ? 'Завантаження...' : 'Увійти'}
              </button>
            </form>
            
            <div className="login-footer">
              <button 
                onClick={handleForgotPassword} 
                className="forgot-password-button"
              >
                Забули пароль?
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;