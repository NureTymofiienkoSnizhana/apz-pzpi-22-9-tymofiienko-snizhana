import React, { useState } from 'react';
import { addDevice } from '../../../services/deviceService';
import './Modal.css';

const AddDeviceModal = ({ onClose, onDeviceAdded }) => {
  const [deviceData, setDeviceData] = useState({
    serial_number: '',
    model: '',
    status: 'inactive'
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setDeviceData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!deviceData.serial_number || !deviceData.model) {
      setError('Будь ласка, заповніть всі обов\'язкові поля');
      return;
    }
    
    setLoading(true);
    try {
      await addDevice(deviceData);
      
      if (onDeviceAdded) onDeviceAdded();
      onClose();
    } catch (err) {
      console.error('Помилка при додаванні пристрою:', err);
      setError('Помилка при додаванні пристрою: ' + 
        (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>Додати новий пристрій</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        <form onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}
          
          <div className="form-group">
            <label htmlFor="serial_number">Серійний номер *</label>
            <input
              type="text"
              id="serial_number"
              name="serial_number"
              value={deviceData.serial_number}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="model">Модель *</label>
            <input
              type="text"
              id="model"
              name="model"
              value={deviceData.model}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="status">Статус</label>
            <select
              id="status"
              name="status"
              value={deviceData.status}
              onChange={handleChange}
            >
              <option value="inactive">Неактивний</option>
              <option value="active">Активний</option>
              <option value="offline">Офлайн</option>
            </select>
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

export default AddDeviceModal;