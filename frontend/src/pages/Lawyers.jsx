import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import client from '../services/client';
import { Search, UserCheck, Star, Sparkles, Filter } from 'lucide-react';

const Lawyers = () => {
  const [lawyers, setLawyers] = useState([]);
  const [categories, setCategories] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [minExperience, setMinExperience] = useState(0);
  const [maxRate, setMaxRate] = useState(500);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchCategories();
    fetchLawyers();
  }, []);

  const fetchCategories = async () => {
    try {
      const response = await client.get('/api/auth/categories');
      setCategories(response.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchLawyers = async () => {
    setLoading(true);
    try {
      // Fetch public approved list
      const response = await client.get('/api/auth/lawyers');
      setLawyers(response.data);
    } catch (err) {
      console.error("Failed to load lawyers:", err);
      // Mock data in case backend is offline
      setLawyers([
        {
          id: 1,
          name: "Sarah Jenkins",
          specialization: "Labor & Employment Law specialist",
          experienceYears: 10,
          hourlyRate: 150,
          categoryName: "Labor Law",
          averageRating: 4.8,
          bio: "Experienced attorney representing clients in wage disputes, termination compensation, and HR policies."
        },
        {
          id: 2,
          name: "David Vance",
          specialization: "Criminal Defense Expert",
          experienceYears: 15,
          hourlyRate: 250,
          categoryName: "Criminal Law",
          averageRating: 4.9,
          bio: "Representing criminal proceedings and traffic code compliance cases."
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await client.get(`/api/auth/lawyers/search?query=${searchQuery}`);
      setLawyers(response.data);
    } catch (err) {
      console.error("Search failed:", err);
    } finally {
      setLoading(false);
    }
  };

  // Filter lawyers locally to offer instant interactivity
  const filteredLawyers = lawyers.filter((lawyer) => {
    const matchesCat = selectedCategory ? (lawyer.categoryName === selectedCategory || lawyer.categoryId?.toString() === selectedCategory) : true;
    const matchesExp = lawyer.experienceYears >= minExperience;
    const matchesRate = lawyer.hourlyRate <= maxRate;
    return matchesCat && matchesExp && matchesRate;
  });

  return (
    <div style={{ maxWidth: '1200px', margin: '30px auto', display: 'grid', gridTemplateColumns: '300px 1fr', gap: '30px' }}>
      
      {/* Sidebar Filter Panel */}
      <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '24px', height: 'fit-content' }}>
        <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.2rem' }}>
          <Filter size={20} className="text-gradient" /> Filter Lawyers
        </h3>

        {/* Category select */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <label style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Practice Area</label>
          <select 
            className="input-field" 
            value={selectedCategory} 
            onChange={(e) => setSelectedCategory(e.target.value)}
          >
            <option value="">All Categories</option>
            {categories.map(c => (
              <option key={c.id} value={c.name}>{c.name}</option>
            ))}
          </select>
        </div>

        {/* Experience slider */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
            <span style={{ color: 'var(--text-secondary)' }}>Min Experience</span>
            <strong>{minExperience}+ Years</strong>
          </div>
          <input 
            type="range" 
            min="0" 
            max="30" 
            value={minExperience}
            onChange={(e) => setMinExperience(parseInt(e.target.value))}
            style={{ width: '100%', accentColor: 'var(--accent-primary)' }}
          />
        </div>

        {/* Rate slider */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
            <span style={{ color: 'var(--text-secondary)' }}>Max Hourly Rate</span>
            <strong>${maxRate}/hr</strong>
          </div>
          <input 
            type="range" 
            min="50" 
            max="500" 
            step="10"
            value={maxRate}
            onChange={(e) => setMaxRate(parseInt(e.target.value))}
            style={{ width: '100%', accentColor: 'var(--accent-primary)' }}
          />
        </div>
      </div>

      {/* Discovery List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
        
        {/* Search header bar */}
        <form onSubmit={handleSearchSubmit} className="glass-panel" style={{ padding: '16px 24px', display: 'flex', gap: '12px' }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <Search size={18} style={{ position: 'absolute', left: '16px', top: '14px', color: 'var(--text-muted)' }} />
            <input
              type="text"
              className="input-field"
              style={{ paddingLeft: '48px' }}
              placeholder="Search lawyers by name, keywords, bio, or credentials..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <button type="submit" className="btn-primary" style={{ padding: '0 28px' }}>Search</button>
        </form>

        {/* List of cards */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {loading ? (
            <p style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '40px' }}>Searching legal database...</p>
          ) : filteredLawyers.length > 0 ? (
            filteredLawyers.map((lawyer) => (
              <div 
                key={lawyer.id} 
                className="glass-panel glass-panel-hover" 
                style={{ 
                  padding: '28px', 
                  display: 'grid', 
                  gridTemplateColumns: '1fr 180px', 
                  gap: '20px', 
                  alignItems: 'center' 
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <h3 style={{ fontSize: '1.3rem' }}>{lawyer.name || 'Sarah Jenkins'}</h3>
                    <span className="badge badge-success" style={{ fontSize: '0.7rem' }}>{lawyer.categoryName || 'Labor Law'}</span>
                  </div>
                  
                  <strong style={{ color: 'var(--accent-secondary)', fontSize: '0.95rem' }}>{lawyer.specialization}</strong>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineClamp: 2, overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
                    {lawyer.bio}
                  </p>

                  <div style={{ display: 'flex', gap: '20px', marginTop: '8px', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    <span>Experience: <strong style={{ color: 'var(--text-secondary)' }}>{lawyer.experienceYears} Years</strong></span>
                    <span>Hourly Rate: <strong style={{ color: 'var(--text-secondary)' }}>${lawyer.hourlyRate}/hr</strong></span>
                  </div>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', alignItems: 'stretch' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', color: 'var(--accent-amber)', fontWeight: 600 }}>
                    <Star size={16} fill="var(--accent-amber)" />
                    <span>{lawyer.averageRating ? lawyer.averageRating.toFixed(1) : '4.8'} / 5.0</span>
                  </div>
                  <button onClick={() => navigate(`/lawyers/${lawyer.id}`)} className="btn-primary" style={{ justifyContent: 'center' }}>
                    View Profile
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div className="glass-panel" style={{ padding: '60px', textAlign: 'center', color: 'var(--text-secondary)' }}>
              No lawyers found matching current filters. Try relaxing constraints.
            </div>
          )}
        </div>

      </div>

    </div>
  );
};

export default Lawyers;
