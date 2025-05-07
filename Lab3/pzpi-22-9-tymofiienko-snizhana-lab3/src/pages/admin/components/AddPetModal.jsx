import React, { useState, useEffect } from 'react';
import { addPet } from '../../../services/petService';
import { getAllUsers } from '../../../services/userService';
import './Modal.css';

const AddPetModal = ({ onClose, onPetAdded }) => {
  const [petData, setPetData] = useState({
    name: '',
    species: '',
    breed: '',
    age: '',
    owner_id: ''
  });
  
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
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
    
    if (!petData.name || !petData.species || !petData.owner_id) {
      setError('Будь ласка, заповніть всі обов\'язкові поля');
      return;
    }
    
    setLoading(true);

     try {
      const petToSubmit = {
        name: petData.name,
        species: petData.species,
        breed: petData.breed,
        age: petData.age,
        owner_id: petData.owner_id 
      };
      
      console.log('Дані для додавання тварини:', petToSubmit);
      await addPet(petToSubmit);
      
      if (onPetAdded) onPetAdded();
      onClose();
    } catch (err) {
      console.error('Помилка при додаванні тварини:', err);
      setError('Помилка при додаванні тварини: ' + 
        (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>Додати нову тварину</h3>
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
            <label htmlFor="owner_id">Власник *</label>
            <select
              id="owner_id"
              name="owner_id"
              value={petData.owner_id}
              onChange={handleChange}
              required
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

export default AddPetModal;