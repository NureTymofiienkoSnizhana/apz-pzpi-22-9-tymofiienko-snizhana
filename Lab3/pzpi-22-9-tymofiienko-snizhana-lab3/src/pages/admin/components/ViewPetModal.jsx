import React, { useState, useEffect } from 'react';
import { getPetInfo, deletePet, updatePet, getPetReport } from '../../../services/petService';
import { getAllUsers } from '../../../services/userService';
import './Modal.css';

const ViewPetModal = ({ petId, onClose, onUpdate, onDelete }) => {
  const [pet, setPet] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [users, setUsers] = useState([]);
  
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState({});

  const [showReportControls, setShowReportControls] = useState(false);
  const [reportData, setReportData] = useState({
    startDate: '',
    endDate: ''
  });
  
  useEffect(() => {
    const fetchPetInfo = async () => {
      try {
        setLoading(true);
        const petData = await getPetInfo(petId);
        setPet(petData);

        const usersData = await getAllUsers();
        setUsers(usersData);

        const userRoleUsers = usersData.filter(user => user.role === "user");
        setUsers(userRoleUsers);
        
        setEditData({
          name: petData.name,
          species: petData.species,
          breed: petData.breed || '',
          age: petData.age || 0,
          owner_Id: petData.ownerId || ''
        });
      } catch (err) {
        setError('Помилка при завантаженні даних про тварину: ' + 
          (err.response?.data?.error || err.message));
      } finally {
        setLoading(false);
      }
    };
    
    fetchPetInfo();
  }, [petId]);
  
  const handleDeletePet = async () => {
    if (window.confirm(`Ви впевнені, що хочете видалити тварину ${pet.name}?`)) {
      try {
        await deletePet(petId);
        if (onDelete) onDelete();
        onClose();
      } catch (err) {
        setError('Помилка при видаленні тварини: ' + 
          (err.response?.data?.error || err.message));
      }
    }
  };
  
  const handleEditStart = () => {
    setIsEditing(true);
  };
  
  const handleSaveChanges = async () => {
    try {
      const updateData = {
        _id: petId, 
        name: editData.name,
        species: editData.species,
        breed: editData.breed,
        age: editData.age,
        owner_id: editData.owner_id 
      };
      
      console.log('Дані для оновлення:', updateData);
      await updatePet(updateData);
      
      const updatedPet = await getPetInfo(petId);
      setPet(updatedPet);
      
      setIsEditing(false);
      
      if (onUpdate) onUpdate();
    } catch (err) {
      setError('Помилка при оновленні даних тварини: ' + 
        (err.response?.data?.error || err.message));
    }
  };
  
  const handleCancelEdit = () => {
    setEditData({
      name: pet.name,
      species: pet.species,
      breed: pet.breed || '',
      age: pet.age || 0,
      owner_Id: pet.ownerId || ''
    });
    setIsEditing(false);
  };
  
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setEditData(prev => ({
      ...prev,
      [name]: name === 'age' ? parseInt(value, 10) || 0 : value
    }));
  };

  const handleReportControlsToggle = () => {
    setShowReportControls(!showReportControls);
    
    if (!showReportControls) {
      const today = new Date();
      const lastMonth = new Date();
      lastMonth.setMonth(lastMonth.getMonth() - 1);
      
      setReportData({
        startDate: formatDateForInput(lastMonth),
        endDate: formatDateForInput(today)
      });
    }
  };
  
  const formatDateForInput = (date) => {
    return date.toISOString().split('T')[0];
  };
  
  const handleReportDataChange = (e) => {
    const { name, value } = e.target;
    setReportData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  const handleGenerateReport = async () => {
    try {
      if (!reportData.startDate || !reportData.endDate) {
        setError('Будь ласка, вкажіть початкову і кінцеву дату для звіту');
        return;
      }
      
      const startTime = Math.floor(new Date(reportData.startDate).getTime() / 1000);
      const endTime = Math.floor(new Date(reportData.endDate).getTime() / 1000);
      
      if (startTime > endTime) {
        setError('Початкова дата не може бути пізніше кінцевої дати');
        return;
      }
      
      await getPetReport(petId, startTime, endTime);
      
      setShowReportControls(false);
      
    } catch (err) {
      setError('Помилка при створенні звіту: ' + 
        (err.response?.data?.error || err.message));
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
        <div className="modal-header">
          <h3>{isEditing ? 'Редагування даних тварини' : 'Інформація про тварину'}</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>

        {error && <div className="error-message">{error}</div>}
        
        <div className="pet-details">
          {isEditing ? (
            <div className="pet-edit-form">
              <div className="form-group">
                <label htmlFor="name">Ім'я:</label>
                <input
                  type="text"
                  id="name"
                  name="name"
                  value={editData.name}
                  onChange={handleInputChange}
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="species">Вид:</label>
                <input
                  type="text"
                  id="species"
                  name="species"
                  value={editData.species}
                  onChange={handleInputChange}
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="breed">Порода:</label>
                <input
                  type="text"
                  id="breed"
                  name="breed"
                  value={editData.breed}
                  onChange={handleInputChange}
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="age">Вік:</label>
                <input
                  type="number"
                  id="age"
                  name="age"
                  min="0"
                  value={editData.age}
                  onChange={handleInputChange}
                />
              </div>

              <div className="form-group">
              <label htmlFor="ownerId">Власник:</label>
              <select
                id="owner_id"
                name="owner_id"
                value={editData.owner_id}
                onChange={handleInputChange}
                className="owner-select"
              >
                <option value="">Оберіть власника</option>
                {users.map(user => (
                  <option key={user.id} value={user.id}>
                    {user.full_name}
                  </option>
                ))}
              </select>
              </div>
            </div>
          ) : (
            <>
              <div className="pet-info-group">
                <h4>Основна інформація</h4>
                <div className="pet-info-row">
                  <span className="label">Ім'я:</span>
                  <span className="value">{pet?.name}</span>
                </div>
                <div className="pet-info-row">
                  <span className="label">Вид:</span>
                  <span className="value">{pet?.species}</span>
                </div>
                <div className="pet-info-row">
                  <span className="label">Порода:</span>
                  <span className="value">{pet?.breed || 'Не вказано'}</span>
                </div>
                <div className="pet-info-row">
                  <span className="label">Вік:</span>
                  <span className="value">{pet?.age || 'Не вказано'}</span>
                </div>
              </div>
              
              <div className="pet-info-group">
                <h4>Власник</h4>
                {pet?.owner ? (
                  <>
                    <div className="pet-info-row">
                      <span className="label">Ім'я:</span>
                      <span className="value">{pet.owner.full_name || 'Не вказано'}</span>
                    </div>
                    <div className="pet-info-row">
                      <span className="label">Email:</span>
                      <span className="value">{pet.owner.email || 'Не вказано'}</span>
                    </div>
                  </>
                ) : (
                  <div className="pet-info-row">
                    <span className="value">Власник не призначений</span>
                  </div>
                )}
              </div>
              
              {pet?.health_data && pet.health_data.length > 0 && (
                <div className="pet-info-group">
                  <h4>Дані про здоров'я</h4>
                  {pet.health_data.map((data, index) => (
                    <div key={index} className="health-data-item">
                      <div className="pet-info-row">
                        <span className="label">Дата:</span>
                        <span className="value">{data.time ? new Date(data.time.T * 1000).toLocaleString() : 'Не вказано'}</span>
                      </div>
                      <div className="pet-info-row">
                        <span className="label">Температура:</span>
                        <span className="value">{data.temperature}°C</span>
                      </div>
                      <div className="pet-info-row">
                        <span className="label">Активність:</span>
                        <span className="value">{data.activity}</span>
                      </div>
                      <div className="pet-info-row">
                        <span className="label">Сон (годин):</span>
                        <span className="value">{data.sleep_hours}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {showReportControls && (
                <div className="pet-info-group report-form">
                  <h4>Створення звіту</h4>
                  <div className="form-group">
                    <label htmlFor="startDate">Початкова дата:</label>
                    <input
                      type="date"
                      id="startDate"
                      name="startDate"
                      value={reportData.startDate}
                      onChange={handleReportDataChange}
                      className="date-picker"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="endDate">Кінцева дата:</label>
                    <input
                      type="date"
                      id="endDate"
                      name="endDate"
                      value={reportData.endDate}
                      onChange={handleReportDataChange}
                      className="date-picker"
                    />
                  </div>
                  <div className="report-actions">
                    <button 
                      className="cancel-button" 
                      onClick={handleReportControlsToggle}
                    >
                      Скасувати
                    </button>
                    <button 
                      className="generate-button" 
                      onClick={handleGenerateReport}
                    >
                      Згенерувати звіт
                    </button>
                  </div>
                </div>
              )}
            </>
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
              <button className="delete-button" onClick={handleDeletePet}>
                Видалити
              </button>
              {!showReportControls && (
                <button className="report-button" onClick={handleReportControlsToggle}>
                  Звіт
                </button>
              )}
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

export default ViewPetModal;