import React, { useState, useEffect } from 'react';
import { useCallback } from 'react';
import debounce from 'lodash/debounce';
import { getAllPets } from '../../services/petService';
import { getAllUsers } from '../../services/userService';
import AddPetModal from './components/AddPetModal';
import ViewPetModal from './components/ViewPetModal';
import './PetsManagement.css';

const PetsManagement = () => {
  const [pets, setPets] = useState([]);
  const [owners, setOwners] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  
  const [showAddModal, setShowAddModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [selectedPet, setSelectedPet] = useState(null);
  
  const fetchPets = async () => {
    try {
      setLoading(true);
      const petsData = await getAllPets();
      const usersData = await getAllUsers();

      const ownersMap = {};
      usersData.forEach(user => {
        ownersMap[user.id || user._id || user.ID] = user.full_name || user.FullName || 'Невідомий власник';
      });
    
      setOwners(ownersMap);
    
      const updatedPets = petsData.map(pet => ({
        ...pet,
        ownerName: pet.ownerId && ownersMap[pet.ownerId] 
          ? ownersMap[pet.ownerId] 
          : (pet.owner && pet.owner.full_name) || '-'
      }));
    
      setPets(updatedPets);
      setError(null);
    } catch (err) {
      console.error('Fetch error:', err);
      setError('Помилка при завантаженні даних про тварин');
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    fetchPets();
  }, []);

  const refreshPetsData = async () => {
    await fetchPets();
  };
  
  const handleAddPet = () => {
    setShowAddModal(true);
  };
  
  const handleViewPet = (pet) => {
    setSelectedPet(pet);
    setShowViewModal(true);
  };
  
  const debouncedSetSearch = useCallback(
    debounce((value) => {
      setDebouncedSearchTerm(value);
    }, 300),
    []
  );
  
  const handleSearchChange = (e) => {
    const value = e.target.value;
    setSearchTerm(value);
    debouncedSetSearch(value);
  };

  const filteredPets = pets.filter((pet) => {
    if (!debouncedSearchTerm) return true;
    if (!searchTerm) return true;
    
    const search = searchTerm.toLowerCase().trim();
    
    return (
      (pet.name && pet.name.toLowerCase().includes(search)) ||
      (pet.species && pet.species.toLowerCase().includes(search)) ||
      (pet.breed && pet.breed.toLowerCase().includes(search)) ||
      (pet.ownerName && pet.ownerName.toLowerCase().includes(search)) ||
      (pet.age !== undefined && pet.age.toString() === search)
    );
  });

  if (loading && pets.length === 0) return <div className="loading">Завантаження...</div>;
  if (error && pets.length === 0) return <div className="error">{error}</div>;
  
  return (
    <div className="pets-management">
      <div className="page-header">
        <h2>Управління тваринами</h2>
        <button onClick={handleAddPet} className="add-button">
          + Додати тварину
        </button>
      </div>
      
      <div className="pets-search">
        <input 
          type="text" 
          placeholder="Пошук тварин..." 
          className="search-input"
          value={searchTerm}
          onChange={handleSearchChange}
        />
      </div>
      
      {error && <div className="error-message">{error}</div>}
      
      <div className="pets-table-container">
        <table className="pets-table">
          <thead>
            <tr>
              <th>Ім'я</th>
              <th>Вид</th>
              <th>Порода</th>
              <th>Вік</th>
              <th>Власник</th>
              <th>Дії</th>
            </tr>
          </thead>
          <tbody>
            {filteredPets.map(pet => (
                <tr key={pet.id}>
                  <td>{pet.name}</td>
                  <td>{pet.species}</td>
                  <td>{pet.breed}</td>
                  <td>{pet.age}</td>
                  <td>{pet.ownerName}</td>
                  <td className="actions">
                    <button 
                      className="action-button view"
                      onClick={() => handleViewPet(pet)}
                      title="Переглянути деталі"
                    >
                      Перегляд
                    </button>
                  </td>
                </tr>
            ))}
          </tbody>
        </table>
      </div>
      
      {showAddModal && (
        <AddPetModal 
          onClose={() => setShowAddModal(false)} 
          onPetAdded={refreshPetsData}
        />
      )}
      
      {showViewModal && selectedPet && (
        <ViewPetModal 
          petId={selectedPet.id}
          pet={selectedPet}
          onClose={() => setShowViewModal(false)}
          onUpdate={refreshPetsData}
          onDelete={refreshPetsData}
        />
      )}
    </div>
  );
};

export default PetsManagement;