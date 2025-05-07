import React, { useState, useEffect } from 'react';
import { updatePet } from '../../../services/petService';
import { getAllUsers } from '../../../services/userService';
import './Modal.css';

const EditPetModal = ({ pet, onClose, onPetUpdated }) => {
  const [petData, setPetData] = useState({
    id: pet.id,
    name: pet.name,
    species: pet.species,
    breed: pet.breed || '',
    age: pet.age || '',
    ownerID: pet.owner?.id || ''
  });
  
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  
  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const data = await getAllUsers();
        const ownerUsers = data.filter(user => user.role === 'user');
        setUsers(ownerUsers);
      } catch (err) {
        setError('Помилка при завантаженні списку користувачів');
      }
    };
    
    fetchUsers();
  }, []);
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setPetData(prev => ({
      ...prev,
      [name]: name === 'age' ? parseInt(value, 10) || '' : value
    }));
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!petData.name || !petData.species || !petData.ownerID) {
      setError('Будь ласка, заповніть всі обов\'язкові поля');
      return;
    }
    
    setLoading(true);
    try {
      await updatePet(petData);
      onPetUpdated();
      onClose();
    } catch (err) {
      setError('Помилка при оновленні тварини: ' + 
        (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };
  
  const filteredUsers = users.filter(user => 
    user.full_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase())
  );
  
  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>Редагувати тварину</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        <form onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}
          
          <div className="form-group">
            <label htmlFor="name">Ім'я тварини *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={petData.name}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="species">Вид тварини *</label>
            <input
              type="text"
              id="species"
              name="species"
              value={petData.species}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="breed">Порода</label>
            <input
              type="text"
              id="breed"
              name="breed"
              value={petData.breed}
              onChange={handleChange}
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="age">Вік</label>
            <input
              type="number"
              id="age"
              name="age"
              min="0"
              max="100"
              value={petData.age}
              onChange={handleChange}
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="owner">Власник *</label>
            <input
              type="text"
              placeholder="Пошук власника..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            
            <div className="users-list">
              {filteredUsers.length > 0 ? (
                filteredUsers.map(user => (
                  <div 
                    key={user.id} 
                    className={`user-item ${petData.ownerID === user.id ? 'selected' : ''}`}
                    onClick={() => setPetData(prev => ({ ...prev, ownerID: user.id }))}
                  >
                    <span>{user.full_name}</span>
                    <small>{user.email}</small>
                  </div>
                ))
              ) : (
                <div className="no-results">Не знайдено користувачів</div>
              )}
            </div>
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

export default EditPetModal;