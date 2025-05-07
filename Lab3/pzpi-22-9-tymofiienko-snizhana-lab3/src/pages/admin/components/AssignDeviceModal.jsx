import React, { useState, useEffect } from 'react';
import { getAllPets } from '../../../services/petService';
import { assignDevice, unassignDevice, getAllDevices } from '../../../services/deviceService';
import './Modal.css';

const AssignDeviceModal = ({ device, onClose, onAssign }) => {
  const [pets, setPets] = useState([]);
  const [availablePets, setAvailablePets] = useState([]);
  const [selectedPetId, setSelectedPetId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  
  const isAssigned = !!device.pet_id;
  
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Отримуємо список всіх тварин
        const petsData = await getAllPets();
        setPets(petsData);
        
        // Отримуємо список всіх пристроїв, щоб знати, які тварини вже мають пристрій
        const devicesData = await getAllDevices();
        
        // Створюємо множину ID тварин, які вже мають пристрій
        const assignedPetIds = new Set();
        devicesData.forEach(dev => {
          if (dev.pet_id && dev.id !== device.id) {
            assignedPetIds.add(dev.pet_id);
          }
        });
        
        // Фільтруємо тварин, залишаючи тільки тих, які не мають пристрою
        const availablePetsData = petsData.filter(pet => !assignedPetIds.has(pet.id));
        setAvailablePets(availablePetsData);
        
      } catch (err) {
        console.error('Помилка при завантаженні даних:', err);
        setError('Помилка при завантаженні списку тварин');
      } finally {
        setLoading(false);
      }
    };
    
    if (!isAssigned) {
      fetchData();
    } else {
      setLoading(false);
    }
  }, [isAssigned, device.id]);
  
  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };
  
  const handlePetSelect = (petId) => {
    setSelectedPetId(petId);
  };
  
  const handleAssign = async () => {
    if (!selectedPetId) {
      setError('Будь ласка, виберіть тварину');
      return;
    }
    
    try {
      setLoading(true);
      await assignDevice(device.id, selectedPetId);
      if (onAssign) onAssign();
      onClose();
    } catch (err) {
      setError('Помилка при призначенні пристрою: ' + (err.message || 'Невідома помилка'));
      setLoading(false);
    }
  };
  
  const handleUnassign = async () => {
    try {
      setLoading(true);
      await unassignDevice(device.id);
      if (onAssign) onAssign();
      onClose();
    } catch (err) {
      setError('Помилка при відв\'язуванні пристрою: ' + (err.message || 'Невідома помилка'));
      setLoading(false);
    }
  };
  
  // Фільтруємо доступних тварин за пошуковим запитом
  const filteredPets = availablePets.filter(pet => 
    pet.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    pet.species.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (pet.breed && pet.breed.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>{isAssigned ? 'Відв\'язати пристрій' : 'Призначити пристрій'}</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        {error && <div className="error-message">{error}</div>}
        
        <div className="device-assign-content">
          {isAssigned ? (
            <div className="unassign-message">
              <p>Ви збираєтесь відв'язати пристрій <strong>{device.serial_number}</strong> від тварини <strong>{device.pet_name}</strong>.</p>
              <p>Після відв'язування пристрій буде переведений в статус "Неактивний".</p>
            </div>
          ) : (
            <>
              <div className="device-info-block">
                <h4 className="device-info-title">Інформація про пристрій</h4>
                <div className="device-info-row">
                  <span className="label">Серійний номер:</span>
                  <span className="value">{device.serial_number}</span>
                </div>
                <div className="device-info-row">
                  <span className="label">Модель:</span>
                  <span className="value">{device.model}</span>
                </div>
              </div>
              
              <div className="search-container">
                <label htmlFor="pet-search" className="search-label">Пошук тварини:</label>
                <input
                  type="text"
                  id="pet-search"
                  className="search-input"
                  placeholder="Введіть ім'я, вид або породу..."
                  value={searchTerm}
                  onChange={handleSearchChange}
                />
              </div>
              
              <div className="pets-list">
                {loading ? (
                  <div className="loading">Завантаження тварин...</div>
                ) : filteredPets.length === 0 ? (
                  <div className="no-results">
                    {searchTerm 
                      ? 'Не знайдено тварин за вашим запитом' 
                      : 'Немає доступних тварин без пристрою'}
                  </div>
                ) : (
                  filteredPets.map(pet => (
                    <div 
                      key={pet.id} 
                      className={`pet-item ${selectedPetId === pet.id ? 'selected' : ''}`}
                      onClick={() => handlePetSelect(pet.id)}
                    >
                      <div className="pet-name">{pet.name}</div>
                      <div className="pet-details">
                        {pet.species} {pet.breed ? `(${pet.breed})` : ''}, вік: {pet.age || 'не вказано'}
                      </div>
                      {pet.owner && (
                        <div className="pet-owner">Власник: {pet.owner.full_name}</div>
                      )}
                    </div>
                  ))
                )}
              </div>
            </>
          )}
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
            type="button" 
            className={isAssigned ? "unassign-button" : "assign-button"}
            onClick={isAssigned ? handleUnassign : handleAssign}
            disabled={loading || (!isAssigned && !selectedPetId)}
          >
            {loading ? 'Завантаження...' : (isAssigned ? 'Відв\'язати' : 'Призначити')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AssignDeviceModal;