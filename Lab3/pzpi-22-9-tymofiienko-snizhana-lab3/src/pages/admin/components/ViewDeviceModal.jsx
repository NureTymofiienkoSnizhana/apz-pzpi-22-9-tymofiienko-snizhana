import React, { useState } from 'react';
import { updateDevice, deleteDevice } from '../../../services/deviceService';
import './Modal.css';

const ViewDeviceModal = ({ device, onClose, onUpdate, onDelete }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState({
    serial_number: device.serial_number,
    model: device.model,
    status: device.status
  });
  const [error, setError] = useState('');
  
  const translateStatus = (status) => {
    const statuses = {
      'active': 'Активний',
      'inactive': 'Неактивний',
      'offline': 'Офлайн'
    };
    
    return statuses[status] || status;
  };

  const handleDeleteDevice = async () => {
    if (window.confirm(`Ви впевнені, що хочете видалити пристрій ${device.serial_number}?`)) {
      try {
        await deleteDevice(device.id);
        if (onDelete) onDelete();
        onClose();
      } catch (err) {
        setError('Помилка при видаленні пристрою: ' + err.message);
      }
    }
  };
  
  const handleEditStart = () => {
    setIsEditing(true);
  };
  
  const handleCancelEdit = () => {
    setEditData({
      serial_number: device.serial_number,
      model: device.model,
      status: device.status
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
      const updateData = {
        id: device.id,
        ...editData
      };
      
      await updateDevice(updateData);
      
      if (onUpdate) onUpdate();
      setIsEditing(false);
    } catch (err) {
      setError('Помилка при оновленні пристрою: ' + err.message);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>{isEditing ? 'Редагування пристрою' : 'Інформація про пристрій'}</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        {error && <div className="error-message">{error}</div>}
        
        <div className="device-details">
          {isEditing ? (
            <div className="device-edit-form">
              <div className="form-group">
                <label htmlFor="serial_number">Серійний номер:</label>
                <input
                  type="text"
                  id="serial_number"
                  name="serial_number"
                  value={editData.serial_number}
                  onChange={handleInputChange}
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="model">Модель:</label>
                <input
                  type="text"
                  id="model"
                  name="model"
                  value={editData.model}
                  onChange={handleInputChange}
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="status">Статус:</label>
                <select
                  id="status"
                  name="status"
                  value={editData.status}
                  onChange={handleInputChange}
                >
                  <option value="inactive">Неактивний</option>
                  <option value="active">Активний</option>
                  <option value="offline">Офлайн</option>
                </select>
              </div>
            </div>
          ) : (
            <>
              <div className="device-info-group">
                <h4>Основна інформація</h4>
                <div className="device-info-row">
                  <span className="label">Серійний номер:</span>
                  <span className="value">{device.serial_number}</span>
                </div>
                <div className="device-info-row">
                  <span className="label">Модель:</span>
                  <span className="value">{device.model}</span>
                </div>
                <div className="device-info-row">
                  <span className="label">Статус:</span>
                  <span className={`value status-badge ${device.status}`}>
                    {translateStatus(device.status)}
                  </span>
                </div>
              </div>
              
              {device.pet_id && (
                <div className="device-info-group">
                  <h4>Призначена тварина</h4>
                  <div className="device-info-row">
                    <span className="label">Ім'я:</span>
                    <span className="value">{device.pet_name}</span>
                  </div>
                  {device.pet_owner && (
                    <div className="device-info-row">
                      <span className="label">Власник:</span>
                      <span className="value">{device.pet_owner}</span>
                    </div>
                  )}
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

export default ViewDeviceModal;