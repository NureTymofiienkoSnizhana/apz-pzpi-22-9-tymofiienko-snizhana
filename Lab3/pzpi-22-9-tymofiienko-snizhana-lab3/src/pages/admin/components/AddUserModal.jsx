import React, { useState } from 'react';
import { createUser } from "../../../services/userService";
import "./Modal.css";

const AddUserModal = ({ onClose, onUserAdded }) => {
  const [userData, setUserData] = useState({
    full_name: '',
    email: '',
    role: 'user'
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setUserData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!userData.full_name || !userData.email) {
      setError('Будь ласка, заповніть всі обов\'язкові поля');
      return;
    }
    
    setLoading(true);
    try {
      await createUser(userData);
      
      if (onUserAdded) onUserAdded();
      onClose();
    } catch (err) {
      console.error('Помилка при створенні користувача:', err);
      setError('Помилка при створенні користувача: ' + 
        (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>Додати нового користувача</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        <form onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}
          
          <div className="form-group">
            <label htmlFor="full_name">Повне ім'я *</label>
            <input
              type="text"
              id="full_name"
              name="full_name"
              value={userData.full_name}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="email">Email *</label>
            <input
              type="email"
              id="email"
              name="email"
              value={userData.email}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="role">Роль *</label>
            <select
              id="role"
              name="role"
              value={userData.role}
              onChange={handleChange}
              required
            >
              <option value="user">Власник</option>
              <option value="admin">Адміністратор</option>
              <option value="vet">Ветеринар</option>
            </select>
          </div>
          
          <div className="form-note">
            <p>Після створення користувача, тимчасовий пароль буде відправлено на вказану електронну адресу.</p>
          </div>
          
          <div className="form-actions">
            <button 
              type="button" 
              className="cancel-button" 
              onClick={onClose}
            >
              Скасувати
            </button>
            <button 
              type="submit" 
              className="submit-button"
              disabled={loading}
            >
              {loading ? 'Збереження...' : 'Зберегти'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddUserModal;