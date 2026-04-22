import { useState, useEffect } from 'react';
import { getUserById, searchUsers } from '../services/userService';
import { getConversations } from '../services/messageService';
import { getUserGroups } from '../services/groupService';

export default function Sidebar({ currentUser, selectedChat, onSelect, onLogout, onCreateGroup, unreadCounts }) {
  const [contacts, setContacts] = useState([]);
  const [groups, setGroups] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    loadContacts();
    loadGroups();
    const interval = setInterval(() => {
      loadContacts();
      loadGroups();
    }, 15000);
    return () => clearInterval(interval);
  }, []);

  const loadContacts = async () => {
    try {
      const res = await getConversations(currentUser.userId);
      const partnerIds = res.data;
      if (partnerIds.length === 0) { setContacts([]); return; }
      const userPromises = partnerIds.map((id) =>
        getUserById(id).then((r) => r.data).catch(() => null)
      );
      const users = (await Promise.all(userPromises)).filter(Boolean);
      setContacts(users);
    } catch (err) {
      console.error('Failed to load contacts:', err);
    }
  };

  const loadGroups = async () => {
    try {
      const res = await getUserGroups(currentUser.userId);
      setGroups(res.data);
    } catch (err) {
      console.error('Failed to load groups:', err);
    }
  };

  const handleSearch = async (e) => {
    const query = e.target.value;
    setSearchQuery(query);
    if (query.trim().length < 2) {
      setSearchResults([]);
      setSearching(false);
      return;
    }
    setSearching(true);
    try {
      const res = await searchUsers(query);
      setSearchResults(res.data.filter((u) => u.id !== currentUser.userId));
    } catch (err) {
      console.error('Search failed:', err);
    }
  };

  const handleSelectSearchResult = (user) => {
    if (!contacts.some((c) => c.id === user.id)) {
      setContacts((prev) => [user, ...prev]);
    }
    onSelect({ ...user, type: 'user' });
    setSearchQuery('');
    setSearchResults([]);
    setSearching(false);
  };

  return (
    <div className="sidebar">
      {/* Header */}
      <div className="sidebar-header">
        <div className="sidebar-brand">
          <div className="logo-icon">💬</div>
          <h2><span>Secure</span>Chat</h2>
        </div>
        <button className="btn-logout" onClick={onLogout}>Logout</button>
      </div>

      {/* Search */}
      <div className="search-box">
        <div className="search-wrapper">
          <span className="search-icon">🔍</span>
          <input
            className="search-input"
            type="text"
            placeholder="Search users..."
            value={searchQuery}
            onChange={handleSearch}
          />
        </div>
      </div>

      {/* Create group */}
      <button className="btn-create-group" onClick={onCreateGroup}>
        <span>+</span> New Group
      </button>

      <div className="user-list">
        {/* Search results */}
        {searching && searchQuery.trim().length >= 2 && (
          <p className="section-label">Search results</p>
        )}
        {searching && searchResults.length === 0 && searchQuery.trim().length >= 2 && (
          <p className="empty-text">No users found</p>
        )}
        {searching && searchResults.map((u) => (
          <div key={u.id} className="user-item" onClick={() => handleSelectSearchResult(u)}>
            <div className="avatar">
              {u.username[0].toUpperCase()}
              {u.online && <div className="online-dot" />}
            </div>
            <div className="user-item-info">
              <div className="user-item-name">{u.username}</div>
              <div className="user-item-preview">{u.online ? 'Online' : 'Offline'}</div>
            </div>
          </div>
        ))}

        {/* Groups */}
        {!searching && groups.length > 0 && (
          <>
            <p className="section-label">Groups</p>
            {groups.map((g) => (
              <div
                key={g.id}
                className={`user-item ${selectedChat?.id === g.id ? 'active' : ''}`}
                onClick={() => onSelect({ ...g, type: 'group', username: g.name })}
              >
                <div className="avatar group-avatar">{g.name[0].toUpperCase()}</div>
                <div className="user-item-info">
                  <div className="user-item-name">{g.name}</div>
                  <div className="user-item-preview">{g.memberCount} members</div>
                </div>
              </div>
            ))}
          </>
        )}

        {/* Contacts */}
        {!searching && (
          <>
            <p className="section-label">Messages</p>
            {contacts.length === 0 && (
              <p className="empty-text">No conversations yet.<br />Search a username to start chatting.</p>
            )}
            {contacts.map((u) => {
              const count = unreadCounts?.[u.id] || 0;
              return (
                <div
                  key={u.id}
                  className={`user-item ${selectedChat?.id === u.id ? 'active' : ''}`}
                  onClick={() => onSelect({ ...u, type: 'user' })}
                >
                  <div className="avatar">
                    {u.username[0].toUpperCase()}
                    {u.online && <div className="online-dot" />}
                  </div>
                  <div className="user-item-info">
                    <div className="user-item-name">{u.username}</div>
                    <div className="user-item-preview">{u.online ? 'Online' : 'Offline'}</div>
                  </div>
                  {count > 0 && <div className="unread-badge">{count}</div>}
                </div>
              );
            })}
          </>
        )}
      </div>
    </div>
  );
}
