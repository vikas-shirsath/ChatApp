import { useState } from 'react';
import { createGroup } from '../services/groupService';
import { searchUsers } from '../services/userService';

export default function CreateGroup({ currentUser, onClose, onGroupCreated }) {
  const [name, setName] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [selectedMembers, setSelectedMembers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSearch = async (e) => {
    const query = e.target.value;
    setSearchQuery(query);
    if (query.trim().length < 2) { setSearchResults([]); return; }
    try {
      const res = await searchUsers(query);
      setSearchResults(
        res.data.filter(
          (u) => u.id !== currentUser.userId && !selectedMembers.some((m) => m.id === u.id)
        )
      );
    } catch (err) {
      console.error('Search failed:', err);
    }
  };

  const addMember = (user) => {
    setSelectedMembers((prev) => [...prev, user]);
    setSearchResults((prev) => prev.filter((u) => u.id !== user.id));
    setSearchQuery('');
  };

  const removeMember = (userId) => {
    setSelectedMembers((prev) => prev.filter((u) => u.id !== userId));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    setError('');
    setLoading(true);
    try {
      const memberIds = selectedMembers.map((m) => m.id);
      const res = await createGroup(name, currentUser.userId, memberIds);
      onGroupCreated(res.data);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create group');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h2 style={{ color: 'var(--text-primary)', marginBottom: '0.25rem', fontSize: '1.25rem' }}>
          Create Group
        </h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
          Add members to start a group conversation
        </p>

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Group Name</label>
            <input type="text" placeholder="e.g. Project Team" value={name} onChange={(e) => setName(e.target.value)} required />
          </div>

          <div className="field">
            <label>Add Members</label>
            <input type="text" placeholder="Search users..." value={searchQuery} onChange={handleSearch} />
          </div>

          {searchResults.length > 0 && (
            <div style={{ marginBottom: '1rem' }}>
              {searchResults.map((u) => (
                <div
                  key={u.id}
                  onClick={() => addMember(u)}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '0.5rem',
                    padding: '0.5rem 0.65rem', cursor: 'pointer', borderRadius: '8px',
                    fontSize: '0.85rem', color: 'var(--text-secondary)',
                    transition: 'background 0.15s',
                  }}
                  onMouseEnter={(e) => (e.currentTarget.style.background = 'var(--bg-hover)')}
                  onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                >
                  <span style={{ color: 'var(--accent-light)', fontWeight: 600 }}>+</span>
                  {u.username}
                </div>
              ))}
            </div>
          )}

          {selectedMembers.length > 0 && (
            <div style={{ marginBottom: '1.25rem' }}>
              <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.5rem', display: 'block' }}>
                Members ({selectedMembers.length})
              </label>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
                {selectedMembers.map((u) => (
                  <span
                    key={u.id}
                    style={{
                      background: 'var(--bg-active)', color: 'var(--accent-light)',
                      padding: '0.3rem 0.7rem', borderRadius: '20px', fontSize: '0.8rem',
                      display: 'flex', alignItems: 'center', gap: '0.4rem',
                      border: '1px solid var(--border)',
                    }}
                  >
                    {u.username}
                    <span onClick={() => removeMember(u.id)} style={{ cursor: 'pointer', color: 'var(--red)', fontWeight: 700, fontSize: '0.9rem' }}>×</span>
                  </span>
                ))}
              </div>
            </div>
          )}

          {error && <p className="error">{error}</p>}

          <div style={{ display: 'flex', gap: '0.65rem' }}>
            <button type="button" className="btn-cancel" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={loading || !name.trim()} style={{ flex: 1 }}>
              {loading ? 'Creating...' : 'Create Group'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
