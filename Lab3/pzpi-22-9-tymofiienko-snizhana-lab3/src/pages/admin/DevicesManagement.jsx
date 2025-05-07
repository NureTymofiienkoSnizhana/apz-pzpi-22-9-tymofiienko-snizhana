import React, { useState, useEffect } from 'react';
import { useCallback } from 'react';
import debounce from 'lodash/debounce';
import { getAllDevices, deleteDevice } from '../../services/deviceService';
import AddDeviceModal from './components/AddDeviceModal';
import ViewDeviceModal from './components/ViewDeviceModal';
import AssignDeviceModal from './components/AssignDeviceModal';
import './DevicesManagement.css';

const DevicesManagement = () => {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  
  const [showAddModal, setShowAddModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedDevice, setSelectedDevice] = useState(null);
  
  const fetchDevices = async () => {
    try {
      setLoading(true);
      const devicesData = await getAllDevices();
      setDevices(devicesData);
      setError(null);
    } catch (err) {
      console.error('Fetch error:', err);
      setError('Помилка при завантаженні даних про пристрої');
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    fetchDevices();
  }, []);

  const refreshDevicesData = async () => {
    await fetchDevices();
  };
  
  const handleAddDevice = () => {
    setShowAddModal(true);
  };
  
  const handleViewDevice = (device) => {
    setSelectedDevice(device);
    setShowViewModal(true);
  };
  
  const handleAssignDevice = (device) => {
    setSelectedDevice(device);
    setShowAssignModal(true);
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

  const filteredDevices = devices.filter((device) => {
    if (!debouncedSearchTerm) return true;
    
    const search = debouncedSearchTerm.toLowerCase().trim();
    
    return (
      (device.serial_number && device.serial_number.toLowerCase().includes(search)) ||
      (device.model && device.model.toLowerCase().includes(search)) ||
      (device.status && device.status.toLowerCase().includes(search)) ||
      (device.pet_name && device.pet_name.toLowerCase().includes(search))
    );
  });

  const getStatusClassName = (status) => {
    switch(status) {
      case 'active':
        return 'status-active';
      case 'inactive':
        return 'status-inactive';
      case 'offline':
        return 'status-offline';
      default:
        return '';
    }
  };

  const translateStatus = (status) => {
    const statuses = {
      'active': 'Активний',
      'inactive': 'Неактивний',
      'offline': 'Офлайн'
    };
    
    return statuses[status] || status;
  };

  if (loading && devices.length === 0) return <div className="loading">Завантаження...</div>;
  if (error && devices.length === 0) return <div className="error">{error}</div>;
  
  return (
    <div className="devices-management">
      <div className="page-header">
        <h2>Управління пристроями</h2>
        <button onClick={handleAddDevice} className="add-button">
          + Додати пристрій
        </button>
      </div>
      
      <div className="devices-search">
        <input 
          type="text" 
          placeholder="Пошук пристроїв..." 
          className="search-input"
          value={searchTerm}
          onChange={handleSearchChange}
        />
      </div>
      
      {error && <div className="error-message">{error}</div>}
      
      <div className="devices-table-container">
        <table className="devices-table">
          <thead>
            <tr>
              <th>Серійний номер</th>
              <th>Модель</th>
              <th>Статус</th>
              <th>Тварина</th>
              <th>Дії</th>
            </tr>
          </thead>
          <tbody>
            {filteredDevices.map(device => (
              <tr key={device.id}>
                <td>{device.serial_number}</td>
                <td>{device.model}</td>
                <td>
                  <span className={`status-badge ${getStatusClassName(device.status)}`}>
                    {translateStatus(device.status)}
                  </span>
                </td>
                <td>{device.pet_name || '—'}</td>
                <td className="actions">
                  <button 
                    className="action-button view"
                    onClick={() => handleViewDevice(device)}
                  >
                    Перегляд
                  </button>
                  <button 
                    className="action-button assign"
                    onClick={() => handleAssignDevice(device)}
                  >
                    {device.pet_id ? 'Відв\'язати' : 'Призначити'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      
      {showAddModal && (
        <AddDeviceModal 
          onClose={() => setShowAddModal(false)} 
          onDeviceAdded={refreshDevicesData}
        />
      )}
      
      {showViewModal && selectedDevice && (
        <ViewDeviceModal 
          device={selectedDevice}
          onClose={() => setShowViewModal(false)}
          onUpdate={refreshDevicesData}
          onDelete={refreshDevicesData}
        />
      )}
      
      {showAssignModal && selectedDevice && (
        <AssignDeviceModal 
          device={selectedDevice}
          onClose={() => setShowAssignModal(false)}
          onAssign={refreshDevicesData}
        />
      )}
    </div>
  );
};

export default DevicesManagement;