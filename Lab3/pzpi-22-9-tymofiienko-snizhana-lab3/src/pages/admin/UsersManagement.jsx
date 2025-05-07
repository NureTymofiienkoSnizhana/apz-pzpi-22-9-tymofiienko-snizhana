import React, { useState, useEffect } from 'react';
import { useCallback } from 'react';
import debounce from 'lodash/debounce';
import { getAllUsers, deleteUser, getUserById } from '../../services/userService';
import AddUserModal from './components/AddUserModal';
import ViewUserModal from './components/ViewUserModal';
import './UsersManagement.css';

const UsersManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  
  const [showAddModal, setShowAddModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  
  const fetchUsers = async () => {
    try {
      setLoading(true);
      const usersData = await getAllUsers();
      setUsers(usersData);
      setError(null);
    } catch (err) {
      console.error('Fetch error:', err);
      setError('Помилка при завантаженні даних про користувачів');
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    fetchUsers();
  }, []);

  const refreshUsersData = async () => {
    await fetchUsers();
  };
  
  const handleAddUser = () => {
    setShowAddModal(true);
  };
  
  const handleViewUser = (user) => {
    setSelectedUser(user);
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

  const filteredUsers = users.filter((user) => {
    if (!debouncedSearchTerm) return true;
    if (!searchTerm) return true;
    
    const search = searchTerm.toLowerCase().trim();
    
    return (
      (user.full_name && user.full_name.toLowerCase().includes(search)) ||
      (user.email && user.email.toLowerCase().includes(search)) ||
      (user.role && user.role.toLowerCase().includes(search))
    );
  });

  if (loading && users.length === 0) return <div className="loading">Завантаження...</div>;
  if (error && users.length === 0) return <div className="error">{error}</div>;
  
  return (
    <div className="users-management">
      <div className="page-header">
        <h2>Управління користувачами</h2>
        <button onClick={handleAddUser} className="add-button">
          + Додати користувача
        </button>
      </div>
      
      <div className="users-search">
        <input 
          type="text" 
          placeholder="Пошук користувачів..." 
          className="search-input"
          value={searchTerm}
          onChange={handleSearchChange}
        />
      </div>
      
      {error && <div className="error-message">{error}</div>}
      
      <div className="users-table-container">
        <table className="users-table">
          <thead>
            <tr>
              <th>Ім'я</th>
              <th>Email</th>
              <th>Роль</th>
              <th>Тварини</th>
              <th>Дії</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.map(user => (
                <tr key={user.id}>
                  <td>{user.full_name}</td>
                  <td>{user.email}</td>
                  <td>{translateRole(user.role)}</td>
                  <td>{user.pets?.length || 0}</td>
                  <td className="actions">
                    <button 
                      className="action-button view"
                      onClick={() => handleViewUser(user)}
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
        <AddUserModal 
          onClose={() => setShowAddModal(false)} 
          onUserAdded={refreshUsersData}
        />
      )}
      
      {showViewModal && selectedUser && (
        <ViewUserModal 
          userId={selectedUser.id}
          user={selectedUser}
          onClose={() => setShowViewModal(false)}
          onUpdate={refreshUsersData}
          onDelete={refreshUsersData}
        />
      )}
    </div>
  );
};

const translateRole = (role) => {
  const roles = {
    'admin': 'Адміністратор',
    'user': 'Власник',
    'vet': 'Ветеринар'
  };
  
  return roles[role] || role;
};

export default UsersManagement;