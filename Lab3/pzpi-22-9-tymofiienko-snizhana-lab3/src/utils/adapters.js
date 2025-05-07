export const adaptPetFromAPI = (pet) => {
  if (!pet) return null;
  
  return {
    id: pet.ID,
    name: pet.Name,
    species: pet.Species,
    breed: pet.Breed,
    age: pet.Age,
    ownerId: pet.OwnerID,
    owner: pet.Owner ? {
      id: pet.Owner.ID,
      full_name: pet.Owner.FullName,
      email: pet.Owner.Email
    } : null,
    health_data: Array.isArray(pet.Health) 
      ? pet.Health.map(h => ({
          id: h.ID,
          pet_id: h.petId,
          temperature: h.Temperature,
          activity: h.Activity,
          sleep_hours: h.SleepHours,
          time: h.Time
        }))
      : []
  };
};
  
export const adaptPetToAPI = (frontendPet) => {
    return {
      id: frontendPet.id,
      name: frontendPet.name,
      species: frontendPet.species,
      breed: frontendPet.breed,
      age: frontendPet.age,
      ownerID: frontendPet.ownerID
    };
};
  
export const adaptUserFromAPI = (user) => {
  if (!user) return null;
  
  const adaptedUser = {
    id: user._id || user.ID || user.id,
    full_name: user.full_name || user.FullName || '',
    email: user.email || user.Email || '',
    role: user.role || user.Role || 'user',
    pets_id: user.pets_id || user.PetsID || []
  };
  
  return adaptedUser;
};
  
export const adaptUserToAPI = (frontendUser) => {
    return {
      id: frontendUser.id,
      full_name: frontendUser.full_name,
      email: frontendUser.email,
      role: frontendUser.role,
      password: frontendUser.password 
    };
};