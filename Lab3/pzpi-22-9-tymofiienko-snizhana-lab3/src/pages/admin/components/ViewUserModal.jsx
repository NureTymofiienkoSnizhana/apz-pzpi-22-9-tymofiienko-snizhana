import React, { useState, useEffect } from 'react';
import { getUserById, deleteUser, updateUserProfile, updateUserAdmin  } from '../../../services/userService';
import { getAllPets } from '../../../services/petService';
import './Modal.css';

const ViewUserModal = ({ userId, onClose, onUpdate, onDelete }) => {
  const [user, setUser] = useState(null);
  const [userPets, setUserPets] = useState([]);
  const [loading, setLoading] = useState(true);

  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState({
    full_name: user?.full_name || '',
    email: user?.email || '',
    role: user?.role || 'user'
  });

  const [error, setError] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const userData = await getUserById(userId);
        setUser(userData);
        
        if (userData.pets_id && userData.pets_id.length > 0) {
          const allPets = await getAllPets();
          const userOwnedPets = allPets.filter(pet => 
            userData.pets_id.includes(pet.id)
          );
          setUserPets(userOwnedPets);
        }
      } catch (err) {
        setError('Помилка при завантаженні даних користувача: ' + 
          (err.response?.data?.error || err.message));
      } finally {
        setLoading(false);
      }
    };
    
    fetchData();
  }, [userId]);
  
  const handleDeleteUser = async () => {
    if (window.confirm(`Ви впевнені, що хочете видалити користувача ${user.full_name}?`)) {
      try {
        await deleteUser(userId);
        if (onDelete) onDelete();
        onClose();
      } catch (err) {
        setError('Помилка при видаленні користувача: ' + 
          (err.response?.data?.error || err.message));
      }
    }
  };
  
  const translateRole = (role) => {
    const roles = {
      'admin': 'Адміністратор',
      'user': 'Власник',
      'vet': 'Ветеринар'
    };
    
    return roles[role] || role;
  };

  const handleEditStart = () => {
    setIsEditing(true);
  };

  const handleCancelEdit = () => {
    setEditData({
      full_name: user.full_name,
      email: user.email,
      role: user.role
    });
    setIsEditing(false);
  };
  
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setEditData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  const handleSaveChanges = async () => {
    try {
      const storedUserData = JSON.parse(localStorage.getItem('userData'));
      
      const isCurrentUser = storedUserData.id === userId;
      
      if (isCurrentUser) {
        await updateUserProfile(editData);
      } else {
        const updateData = {
          _id: user.id,
          ...editData
        };
        await updateUserAdmin(updateData);
      }
      
      if (onUpdate) onUpdate();
      setIsEditing(false);
    } catch (err) {
      setError('Помилка при оновленні користувача: ' + err.message);
    }
  };

  if (loading) {
    return (
      <div className="modal-overlay">
        <div className="modal-content">
          <div className="modal-header">
            <h3>Завантаження...</h3>
            <button className="close-button" onClick={onClose}>×</button>
          </div>
          <div className="loading-spinner">Завантаження даних...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header" style={{ backgroundColor: '#4CAF50' }}>
          <h3>Інформація про користувача</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        {error && <div className="error-message">{error}</div>}
        
        <div className="user-details">
          {isEditing ? (
            <div className="user-edit-form">
              <div className="form-group">
                <label htmlFor="full_name">Ім'я:</label>
                <input
                  type="text"
                  id="full_name"
                  name="full_name"
                  value={editData.full_name}
                  onChange={handleInputChange}
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="email">Email:</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={editData.email}
                  onChange={handleInputChange}
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="role">Роль:</label>
                <select
                  id="role"
                  name="role"
                  value={editData.role}
                  onChange={handleInputChange}
                >
                  <option value="user">Власник</option>
                  <option value="admin">Адміністратор</option>
                  <option value="vet">Ветеринар</option>
                </select>
              </div>
            </div>
          ) : (
            <div className="user-info-group">
              <h4>Основна інформація</h4>
              <div className="user-info-row">
                <span className="label">Ім'я:</span>
                <span className="value">{user?.full_name}</span>
              </div>
              <div className="user-info-row">
                <span className="label">Email:</span>
                <span className="value">{user?.email}</span>
              </div>
              <div className="user-info-row">
                <span className="label">Роль:</span>
                <span className="value">{translateRole(user?.role)}</span>
              </div>
            </div>
          )}
          
          {!isEditing && user?.pets && user.pets.length > 0 && (
            <div className="user-info-group">
              <h4>Тварини</h4>
              <div className="user-info-row">
                <span className="value">Кількість тварин: {user.pets.length}</span>
              </div>
            </div>
          )}
        </div>
        
        <div className="form-actions">
          {isEditing ? (
            <>
              <button className="cancel-button" onClick={handleCancelEdit}>
                Скасувати
              </button>
              <button className="save-button" onClick={handleSaveChanges}>
                Зберегти
              </button>
            </>
          ) : (
            <>
              <button className="edit-button" onClick={handleEditStart}>
                Редагувати
              </button>
              <button className="delete-button" onClick={handleDeleteUser}>
                Видалити
              </button>
              <button className="cancel-button" onClick={onClose}>
                Закрити
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ViewUserModal;