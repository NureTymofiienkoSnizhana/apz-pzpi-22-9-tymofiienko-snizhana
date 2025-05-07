import React, { useState, useEffect } from 'react';
import { getUserInfo, updateUserProfile } from '../../services/userService';
import { useAuth } from '../../context/AuthContext';
import './UserProfile.css';

const UserProfile = () => {
  const { currentUser } = useAuth();
  const [userProfile, setUserProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    full_name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [formErrors, setFormErrors] = useState({});
  
  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        setLoading(true);
        
        const userProfileData = await getUserInfo();
        setUserProfile(userProfileData);
        
        setFormData({
          full_name: userProfileData.FullName || '', 
          email: userProfileData.Email || '',       
          password: '',
          confirmPassword: ''
        });
      } catch (err) {
        console.error('Помилка при отриманні даних профілю:', err);
        setError('Не вдалося завантажити дані профілю: ' + err.message);
      } finally {
        setLoading(false);
      }
    };
    
    fetchUserProfile();
  }, []);
  
  const handleEditToggle = () => {
    setIsEditing(!isEditing);
    
    if (!isEditing) {
      setFormData(prev => ({
        ...prev,
        password: '',
        confirmPassword: ''
      }));
      setFormErrors({});
    }
  };
  
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    if (formErrors[name]) {
      setFormErrors(prev => ({
        ...prev,
        [name]: null
      }));
    }
  };
  
  const validateForm = () => {
    const errors = {};
    
    if (!formData.full_name.trim()) {
      errors.full_name = 'Ім\'я користувача обов\'язкове';
    }
    
    if (!formData.email.trim()) {
      errors.email = 'Email обов\'язковий';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      errors.email = 'Введіть коректний email';
    }
    
    if (formData.password && formData.password.length < 6) {
      errors.password = 'Пароль повинен містити не менше 6 символів';
    }
    
    if (formData.password && formData.password !== formData.confirmPassword) {
      errors.confirmPassword = 'Паролі не співпадають';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    try {
      const updateData = {
        full_name: formData.full_name, 
        email: formData.email,       
      };
      
      if (formData.password) {
        updateData.password = formData.password;
      }
      
      await updateUserProfile(updateData);
      
      const updatedUserData = await getUserInfo();
      setUserProfile(updatedUserData);
      
      setIsEditing(false);
      alert('Профіль успішно оновлено');
    } catch (err) {
      console.error('Помилка при оновленні профілю:', err);
      setFormErrors(prev => ({
        ...prev,
        general: 'Не вдалося оновити профіль. ' + (err.response?.data?.error || err.message)
      }));
    }
  };
  
  if (loading) {
    return <div className="profile-container loading">Завантаження даних профілю...</div>;
  }
  
  if (error) {
    return (
      <div className="profile-container error">
        <p>{error}</p>
        <button 
          onClick={() => window.location.href = '/login'} 
          className="login-button"
        >
          Перейти на сторінку входу
        </button>
      </div>
    );
  }

  if (!userProfile) {
    return (
      <div className="profile-container error">
        <p>Інформацію про профіль не знайдено. Можливо, вам потрібно увійти знову.</p>
        <button 
          onClick={() => window.location.href = '/login'} 
          className="login-button"
        >
          Перейти на сторінку входу
        </button>
      </div>
    );
  }
  
  return (
    <div className="profile-container">
      <div className="profile-header">
        <h2>Мій профіль</h2>
        <button 
          onClick={handleEditToggle} 
          className={`edit-profile-button ${isEditing ? 'active' : ''}`}
        >
          {isEditing ? 'Скасувати' : 'Редагувати профіль'}
        </button>
      </div>
      
      {isEditing ? (
        <form className="profile-edit-form" onSubmit={handleSubmit}>
          {formErrors.general && (
            <div className="error-message">{formErrors.general}</div>
          )}
          
          <div className="form-group">
            <label htmlFor="full_name">Повне ім'я:</label>
            <input
              type="text"
              id="full_name"
              name="full_name"
              value={formData.full_name}
              onChange={handleInputChange}
              className={formErrors.full_name ? 'error' : ''}
            />
            {formErrors.full_name && (
              <div className="field-error">{formErrors.full_name}</div>
            )}
          </div>
          
          <div className="form-group">
            <label htmlFor="email">Email:</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              className={formErrors.email ? 'error' : ''}
            />
            {formErrors.email && (
              <div className="field-error">{formErrors.email}</div>
            )}
          </div>
          
          <div className="form-group">
            <label htmlFor="password">Новий пароль (залиште порожнім, щоб не змінювати):</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              className={formErrors.password ? 'error' : ''}
            />
            {formErrors.password && (
              <div className="field-error">{formErrors.password}</div>
            )}
          </div>
          
          <div className="form-group">
            <label htmlFor="confirmPassword">Підтвердження паролю:</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              className={formErrors.confirmPassword ? 'error' : ''}
              disabled={!formData.password}
            />
            {formErrors.confirmPassword && (
              <div className="field-error">{formErrors.confirmPassword}</div>
            )}
          </div>
          
          <div className="form-actions">
            <button type="button" className="cancel-button" onClick={handleEditToggle}>
              Скасувати
            </button>
            <button type="submit" className="save-button">
              Зберегти зміни
            </button>
          </div>
        </form>
      ) : (
        <div className="profile-info">
          <div className="profile-section">
            <h3>Особисті дані</h3>
            <div className="profile-row">
              <span className="profile-label">Повне ім'я:</span>
              <span className="profile-value">{userProfile.FullName}</span>
            </div>
            <div className="profile-row">
              <span className="profile-label">Email:</span>
              <span className="profile-value">{userProfile.Email}</span>
            </div>
            <div className="profile-row">
              <span className="profile-label">Роль:</span>
              <span className="profile-value role-badge">
                {userProfile.Role === 'admin' ? 'Адміністратор' : 
                 userProfile.Role === 'vet' ? 'Ветеринар' : 'Користувач'}
              </span>
            </div>
          </div>
          
          {userProfile.PetsID && userProfile.PetsID.length > 0 && (
            <div className="profile-section">
              <h3>Мої тварини</h3>
              <div className="pets-count">
                Кількість тварин: <strong>{userProfile.PetsID.length}</strong>
              </div>
              <button 
                className="view-pets-button"
                onClick={() => window.location.href = '/admin/pets'}
              >
                Переглянути моїх тварин
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default UserProfile;