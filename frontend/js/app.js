// Global State Cache
let state = {
    books: [],
    users: [],
    records: [],
    activeTab: 'dashboard'
};

// Config
const API_BASE = 'http://localhost:8080/api';

// ==========================================
// INITIALIZATION & TAB ROUTING
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    initApp();
    setupEventListeners();
});

function initApp() {
    // Initial data load
    refreshAllData();

    // Setup sidebar navigation clicks
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            const tabId = item.getAttribute('data-tab');
            switchTab(tabId);
        });
    });

    // Mobile nav toggle
    const mobileToggle = document.getElementById('mobile-toggle');
    const sidebar = document.getElementById('sidebar');
    if (mobileToggle && sidebar) {
        mobileToggle.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }
}

function switchTab(tabId) {
    state.activeTab = tabId;

    // Toggle active classes in navigation
    document.querySelectorAll('.nav-item').forEach(item => {
        if (item.getAttribute('data-tab') === tabId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Toggle active tab content pane
    document.querySelectorAll('.tab-content').forEach(pane => {
        if (pane.id === `${tabId}-tab`) {
            pane.classList.add('active');
        } else {
            pane.classList.remove('active');
        }
    });

    // Close mobile sidebar if open
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        sidebar.classList.remove('active');
    }

    // Update Header Titles
    const pageTitle = document.getElementById('page-title');
    const pageSubtitle = document.getElementById('page-subtitle');
    
    switch (tabId) {
        case 'dashboard':
            pageTitle.textContent = 'Dashboard Overview';
            pageSubtitle.textContent = "Welcome back, Admin. Here is today's summary.";
            refreshDashboard();
            break;
        case 'books':
            pageTitle.textContent = 'Book Management';
            pageSubtitle.textContent = 'Add, edit, remove, and search books in the catalogue.';
            renderBooksTable();
            break;
        case 'users':
            pageTitle.textContent = 'User Directory';
            pageSubtitle.textContent = 'Register new readers, edit credentials, and track memberships.';
            renderUsersTable();
            break;
        case 'transactions':
            pageTitle.textContent = 'Borrow & Return Center';
            pageSubtitle.textContent = 'Issue books, manage returns, apply renewals, and place holds.';
            populateTransactionDropdowns();
            break;
        case 'records':
            pageTitle.textContent = 'Borrowing Registry';
            pageSubtitle.textContent = 'Global log history of active borrow transactions and late fees.';
            renderRecordsTable();
            break;
    }
}

// Helper to fetch all state arrays from backend APIs
function refreshAllData() {
    Promise.all([
        fetch(`${API_BASE}/books`).then(res => res.json()),
        fetch(`${API_BASE}/users`).then(res => res.json()),
        fetch(`${API_BASE}/records`).then(res => res.json())
    ]).then(([booksRes, usersRes, recordsRes]) => {
        if (booksRes.success) state.books = booksRes.books;
        if (usersRes.success) state.users = usersRes.users;
        if (recordsRes.success) state.records = recordsRes.records;

        // Populate dynamic genre lists and dropdowns
        populateGenreDropdown();

        // Refresh currently active tab visual
        switchTab(state.activeTab);
    }).catch(err => {
        console.error('API Data sync failed:', err);
        showToast('Sync Error', 'Failed to synchronize with database. Check API server status.', 'error');
    });
}

// ==========================================
// TOAST NOTIFICATIONS ENGINE
// ==========================================
function showToast(title, message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let iconSvg = '';
    if (type === 'success') {
        iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14M22 4L12 14.01l-3-3"/></svg>';
    } else if (type === 'error') {
        iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>';
    } else {
        iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>';
    }

    toast.innerHTML = `
        <div class="toast-icon">${iconSvg}</div>
        <div class="toast-content">
            <div class="toast-title">${title}</div>
            <div>${message}</div>
        </div>
    `;

    container.appendChild(toast);

    // Auto dismiss after 4 seconds
    setTimeout(() => {
        toast.classList.add('fade-out');
        toast.addEventListener('animationend', () => {
            toast.remove();
        });
    }, 4000);
}

// ==========================================
// MODAL CONTROLS
// ==========================================
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
    }
}

// ==========================================
// TAB: DASHBOARD FUNCTIONS
// ==========================================
function refreshDashboard() {
    fetch(`${API_BASE}/dashboard`)
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                document.getElementById('stat-total-books').textContent = data.totalBooks;
                document.getElementById('stat-avail-books').textContent = data.availableBooks;
                document.getElementById('stat-borrowed-books').textContent = data.borrowedBooks;
                document.getElementById('stat-total-users').textContent = data.totalUsers;
                document.getElementById('stat-active-borrows').textContent = data.activeBorrows;

                // Render activities
                const activityList = document.getElementById('recent-activity-list');
                activityList.innerHTML = '';
                
                if (data.recentActivity.length === 0) {
                    activityList.innerHTML = '<p class="timeline-content" style="text-align: center; padding: 20px;">No recent activities logged.</p>';
                } else {
                    data.recentActivity.forEach(activity => {
                        const item = document.createElement('div');
                        item.className = 'timeline-item';
                        item.innerHTML = `
                            <div class="timeline-bullet"></div>
                            <div class="timeline-content">${activity}</div>
                        `;
                        activityList.appendChild(item);
                    });
                }
            }
        })
        .catch(err => console.error('Dashboard reload failed:', err));
}

// ==========================================
// TAB: BOOKS CATALOGUE FUNCTIONS
// ==========================================
function populateGenreFilterOptions() {
    const genreFilter = document.getElementById('book-genre-filter');
    const prevVal = genreFilter.value;
    genreFilter.innerHTML = '<option value="">All Genres</option>';
    
    // Deduplicate genres
    const genres = [...new Set(state.books.map(b => b.genre))].filter(Boolean);
    genres.sort().forEach(genre => {
        const opt = document.createElement('option');
        opt.value = genre;
        opt.textContent = genre;
        genreFilter.appendChild(opt);
    });

    genreFilter.value = prevVal;
}

function populateGenreDropdown() {
    populateGenreFilterOptions();
}

function renderBooksTable() {
    const tableBody = document.getElementById('books-table-body');
    tableBody.innerHTML = '';

    const searchQuery = document.getElementById('book-search-input').value.toLowerCase();
    const genreFilter = document.getElementById('book-genre-filter').value;
    const statusFilter = document.getElementById('book-status-filter').value;

    const filteredBooks = state.books.filter(book => {
        const matchesSearch = book.title.toLowerCase().includes(searchQuery) ||
                              book.author.toLowerCase().includes(searchQuery) ||
                              book.isbn.toLowerCase().includes(searchQuery);
        const matchesGenre = !genreFilter || book.genre === genreFilter;
        const matchesStatus = !statusFilter || book.status === statusFilter;
        return matchesSearch && matchesGenre && matchesStatus;
    });

    if (filteredBooks.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 40px; color: var(--text-muted);">No books found matching criteria.</td></tr>';
        return;
    }

    filteredBooks.forEach(book => {
        const tr = document.createElement('tr');
        
        let statusBadgeClass = 'badge-available';
        if (book.status === 'BORROWED') {
            statusBadgeClass = 'badge-borrowed';
        }
        
        // Find if it is on hold
        const isHold = state.records.some(r => r.isbn === book.isbn && r.onHold);
        const badgeLabel = isHold ? 'ON HOLD' : book.status;
        const badgeClass = isHold ? 'badge-hold' : statusBadgeClass;

        tr.innerHTML = `
            <td style="font-weight: 600; color: var(--text-primary);">${book.isbn}</td>
            <td style="color: var(--text-primary); font-weight: 500;">${book.title}</td>
            <td>${book.author}</td>
            <td>${book.genre}</td>
            <td><span class="badge ${badgeClass}">${badgeLabel}</span></td>
            <td style="text-align: right;">
                <div class="action-btn-group" style="justify-content: flex-end;">
                    <button class="btn-icon" title="Edit book" onclick="editBookTrigger('${book.isbn}')">
                        <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4z"/></svg>
                    </button>
                    <button class="btn-icon btn-delete" title="Delete book" onclick="deleteBookTrigger('${book.isbn}')">
                        <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                    </button>
                </div>
            </td>
        `;
        tableBody.appendChild(tr);
    });
}

function editBookTrigger(isbn) {
    const book = state.books.find(b => b.isbn === isbn);
    if (!book) return;

    // Prefill modal form
    document.getElementById('book-modal-title').textContent = 'Edit Book Details';
    const isbnInput = document.getElementById('book-isbn');
    isbnInput.value = book.isbn;
    isbnInput.disabled = true; // Key field, cannot be modified
    
    document.getElementById('book-title').value = book.title;
    document.getElementById('book-author').value = book.author;
    document.getElementById('book-genre').value = book.genre;

    openModal('book-modal');
}

function deleteBookTrigger(isbn) {
    if (confirm(`Are you sure you want to delete the book with ISBN ${isbn}?`)) {
        fetch(`${API_BASE}/books/delete`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ isbn: isbn })
        })
        .then(res => res.json())
        .then(res => {
            if (res.success) {
                showToast('Deleted Book', res.message, 'success');
                refreshAllData();
            } else {
                showToast('Failed to Delete', res.message, 'error');
            }
        })
        .catch(err => {
            console.error('Delete book failed:', err);
            showToast('API Error', 'Failed to communicate deletion to server.', 'error');
        });
    }
}

// ==========================================
// TAB: USER DIRECTORY FUNCTIONS
// ==========================================
function renderUsersTable() {
    const tableBody = document.getElementById('users-table-body');
    tableBody.innerHTML = '';

    const searchQuery = document.getElementById('user-search-input').value.toLowerCase();

    const filteredUsers = state.users.filter(user => {
        return user.name.toLowerCase().includes(searchQuery) ||
               String(user.userId).includes(searchQuery);
    });

    if (filteredUsers.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 40px; color: var(--text-muted);">No users found matching criteria.</td></tr>';
        return;
    }

    filteredUsers.forEach(user => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="font-weight: 600; color: var(--text-primary);">#${user.userId}</td>
            <td style="color: var(--text-primary); font-weight: 500;">${user.name}</td>
            <td>${user.contact}</td>
            <td>
                <span class="badge ${user.borrowedCount >= 3 ? 'badge-hold' : 'badge-available'}">
                    ${user.borrowedCount} / 3 Books
                </span>
            </td>
            <td style="text-align: right;">
                <div class="action-btn-group" style="justify-content: flex-end;">
                    <button class="btn-icon" title="Edit user" onclick="editUserTrigger(${user.userId})">
                        <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4z"/></svg>
                    </button>
                    <button class="btn-icon btn-delete" title="Delete user" onclick="deleteUserTrigger(${user.userId})">
                        <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                    </button>
                </div>
            </td>
        `;
        tableBody.appendChild(tr);
    });
}

function editUserTrigger(userId) {
    const user = state.users.find(u => u.userId === userId);
    if (!user) return;

    document.getElementById('user-modal-title').textContent = 'Edit User Details';
    const idInput = document.getElementById('user-id');
    idInput.value = user.userId;
    idInput.disabled = true; // Key field, cannot be modified
    
    document.getElementById('user-name').value = user.name;
    document.getElementById('user-contact').value = user.contact;

    openModal('user-modal');
}

function deleteUserTrigger(userId) {
    if (confirm(`Are you sure you want to delete User ID #${userId}?`)) {
        fetch(`${API_BASE}/users/delete`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: userId })
        })
        .then(res => res.json())
        .then(res => {
            if (res.success) {
                showToast('Deleted User', res.message, 'success');
                refreshAllData();
            } else {
                showToast('Failed to Delete', res.message, 'error');
            }
        })
        .catch(err => {
            console.error('Delete user failed:', err);
            showToast('API Error', 'Failed to communicate deletion to server.', 'error');
        });
    }
}

// ==========================================
// TAB: BORROW / RETURN FUNCTIONS
// ==========================================
function populateTransactionDropdowns() {
    const borrowUserSelect = document.getElementById('borrow-user-select');
    const borrowBookSelect = document.getElementById('borrow-book-select');
    const activeRecordSelect = document.getElementById('active-record-select');
    const holdUserSelect = document.getElementById('hold-user-select');

    // Save previous selections
    const prevBorrowUser = borrowUserSelect.value;
    const prevBorrowBook = borrowBookSelect.value;
    const prevActiveRec = activeRecordSelect.value;
    const prevHoldUser = holdUserSelect.value;

    // Reset dropdowns
    borrowUserSelect.innerHTML = '<option value="" disabled selected>Choose a user...</option>';
    borrowBookSelect.innerHTML = '<option value="" disabled selected>Choose a book...</option>';
    activeRecordSelect.innerHTML = '<option value="" disabled selected>Choose active record...</option>';
    holdUserSelect.innerHTML = '<option value="" disabled selected>Select user for hold...</option>';

    // 1. Populate Users in Borrow User selector
    state.users.forEach(user => {
        const opt = document.createElement('option');
        opt.value = user.userId;
        opt.textContent = `${user.name} (ID: ${user.userId}) - Borrowed: ${user.borrowedCount}/3`;
        borrowUserSelect.appendChild(opt);
    });

    // 2. Populate Available Books in Borrow Book selector
    state.books.forEach(book => {
        if (book.status === 'AVAILABLE') {
            const opt = document.createElement('option');
            opt.value = book.isbn;
            opt.textContent = `${book.title} (ISBN: ${book.isbn}) - ${book.genre}`;
            borrowBookSelect.appendChild(opt);
        }
    });

    // 3. Populate Active Records in Return selector
    state.records.forEach(rec => {
        const opt = document.createElement('option');
        opt.value = `${rec.userId}_${rec.isbn}`;
        opt.textContent = `${rec.userName} (ID: ${rec.userId}) -> ${rec.bookTitle} (${rec.isbn})`;
        activeRecordSelect.appendChild(opt);
    });

    // 4. Populate Users for placing Holds (can hold any book they haven't borrowed)
    state.users.forEach(user => {
        const opt = document.createElement('option');
        opt.value = user.userId;
        opt.textContent = `${user.name} (ID: ${user.userId})`;
        holdUserSelect.appendChild(opt);
    });

    // Restore previous selections where applicable
    if (prevBorrowUser) borrowUserSelect.value = prevBorrowUser;
    if (prevBorrowBook) borrowBookSelect.value = prevBorrowBook;
    if (prevActiveRec) activeRecordSelect.value = prevActiveRec;
    if (prevHoldUser) holdUserSelect.value = prevHoldUser;

    // Trigger preview boxes recalculation
    updateBorrowPreview();
    updateRecordPreview();
}

function updateBorrowPreview() {
    const borrowUserSelect = document.getElementById('borrow-user-select');
    const borrowBookSelect = document.getElementById('borrow-book-select');
    const previewBox = document.getElementById('borrow-preview-box');

    if (!borrowUserSelect.value || !borrowBookSelect.value) {
        previewBox.classList.remove('active');
        return;
    }

    const userId = parseInt(borrowUserSelect.value);
    const user = state.users.find(u => u.userId === userId);

    if (user) {
        document.getElementById('borrow-preview-user-count').textContent = `${user.borrowedCount} / 3 Books`;
        
        // Highlight in red if limit reached
        if (user.borrowedCount >= 3) {
            document.getElementById('borrow-preview-user-count').className = 'info-value danger';
        } else {
            document.getElementById('borrow-preview-user-count').className = 'info-value success';
        }

        // Calculate 14 days due date
        const today = new Date();
        const due = new Date();
        due.setDate(today.getDate() + 14);
        
        const dateStr = due.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
        document.getElementById('borrow-preview-due-date').textContent = dateStr;
        
        previewBox.classList.add('active');
    }
}

function updateRecordPreview() {
    const recordSelect = document.getElementById('active-record-select');
    const previewBox = document.getElementById('record-preview-box');

    if (!recordSelect.value) {
        previewBox.classList.remove('active');
        return;
    }

    const [userIdStr, isbn] = recordSelect.value.split('_');
    const userId = parseInt(userIdStr);

    const rec = state.records.find(r => r.userId === userId && r.isbn === isbn);

    if (rec) {
        document.getElementById('record-preview-date').textContent = rec.borrowDate;
        document.getElementById('record-preview-due').textContent = rec.dueDate;
        
        document.getElementById('record-preview-renewed').textContent = rec.renewed ? 'Yes (Extended)' : 'No';
        document.getElementById('record-preview-renewed').className = rec.renewed ? 'info-value danger' : 'info-value success';
        
        document.getElementById('record-preview-hold').textContent = rec.onHold ? 'Yes (Hold Placed)' : 'No';
        document.getElementById('record-preview-hold').className = rec.onHold ? 'info-value danger' : 'info-value success';

        // Calculate late fee dynamically (based on late days, 10 Rs per day)
        const dueDateObj = new Date(rec.dueDate);
        const todayObj = new Date();
        // Reset hours for accurate date diff
        dueDateObj.setHours(0,0,0,0);
        todayObj.setHours(0,0,0,0);

        let fee = 0;
        if (todayObj > dueDateObj) {
            const timeDiff = todayObj.getTime() - dueDateObj.getTime();
            const diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
            fee = diffDays * 10.0;
        }

        const feeElement = document.getElementById('record-preview-fee');
        feeElement.textContent = `Rs. ${fee.toFixed(2)}`;
        if (fee > 0) {
            feeElement.className = 'info-value danger';
        } else {
            feeElement.className = 'info-value success';
        }

        previewBox.classList.add('active');
    }
}

// ==========================================
// TAB: BORROW REGISTRY HISTORY FUNCTIONS
// ==========================================
function renderRecordsTable() {
    const tableBody = document.getElementById('records-table-body');
    tableBody.innerHTML = '';

    const searchQuery = document.getElementById('record-search-input').value.toLowerCase();
    const renewFilter = document.getElementById('record-renew-filter').value;
    const holdFilter = document.getElementById('record-hold-filter').value;

    const filteredRecords = state.records.filter(rec => {
        const matchesSearch = rec.userName.toLowerCase().includes(searchQuery) ||
                              String(rec.userId).includes(searchQuery) ||
                              rec.bookTitle.toLowerCase().includes(searchQuery) ||
                              rec.isbn.toLowerCase().includes(searchQuery);
        const matchesRenew = !renewFilter || String(rec.renewed) === renewFilter;
        const matchesHold = !holdFilter || String(rec.onHold) === holdFilter;
        return matchesSearch && matchesRenew && matchesHold;
    });

    if (filteredRecords.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 40px; color: var(--text-muted);">No borrow records found.</td></tr>';
        return;
    }

    filteredRecords.forEach(rec => {
        // Compute fee
        const dueObj = new Date(rec.dueDate);
        const todayObj = new Date();
        dueObj.setHours(0,0,0,0);
        todayObj.setHours(0,0,0,0);
        let lateFee = 0;
        if (todayObj > dueObj) {
            const days = Math.ceil((todayObj.getTime() - dueObj.getTime()) / (1000 * 3600 * 24));
            lateFee = days * 10;
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div style="font-weight: 600; color: var(--text-primary);">${rec.userName}</div>
                <div style="font-size: 11px; color: var(--text-muted);">ID: #${rec.userId}</div>
            </td>
            <td>
                <div style="font-weight: 500; color: var(--text-primary);">${rec.bookTitle}</div>
                <div style="font-size: 11px; color: var(--text-muted);">ISBN: ${rec.isbn}</div>
            </td>
            <td>${rec.borrowDate}</td>
            <td style="color: ${lateFee > 0 ? 'var(--status-borrowed)' : 'inherit'}; font-weight: ${lateFee > 0 ? '600' : 'normal'};">${rec.dueDate}</td>
            <td>
                <span class="badge ${rec.renewed ? 'badge-borrowed' : 'badge-available'}">
                    ${rec.renewed ? 'RENEWED' : 'STANDARD'}
                </span>
            </td>
            <td>
                <span class="badge ${rec.onHold ? 'badge-hold' : 'badge-available'}">
                    ${rec.onHold ? 'ON HOLD' : 'NONE'}
                </span>
            </td>
            <td style="font-weight: 600; color: ${lateFee > 0 ? 'var(--status-borrowed)' : 'var(--status-avail)'}">
                Rs. ${lateFee.toFixed(2)}
            </td>
        `;
        tableBody.appendChild(tr);
    });
}

// ==========================================
// SUBMISSIONS & ACTION HANDLERS
// ==========================================
function setupEventListeners() {
    // 1. Filter and search updates
    document.getElementById('book-search-input').addEventListener('input', renderBooksTable);
    document.getElementById('book-genre-filter').addEventListener('change', renderBooksTable);
    document.getElementById('book-status-filter').addEventListener('change', renderBooksTable);

    document.getElementById('user-search-input').addEventListener('input', renderUsersTable);

    document.getElementById('record-search-input').addEventListener('input', renderRecordsTable);
    document.getElementById('record-renew-filter').addEventListener('change', renderRecordsTable);
    document.getElementById('record-hold-filter').addEventListener('change', renderRecordsTable);

    // 2. Select change preview bindings
    document.getElementById('borrow-user-select').addEventListener('change', updateBorrowPreview);
    document.getElementById('borrow-book-select').addEventListener('change', updateBorrowPreview);
    document.getElementById('active-record-select').addEventListener('change', updateRecordPreview);

    // 3. Quick Action Buttons
    document.getElementById('action-quick-borrow').addEventListener('click', () => switchTab('transactions'));
    document.getElementById('action-quick-book').addEventListener('click', () => {
        document.getElementById('book-modal-title').textContent = 'Add New Book';
        const isbnInput = document.getElementById('book-isbn');
        isbnInput.disabled = false;
        document.getElementById('book-modal-form').reset();
        openModal('book-modal');
    });
    document.getElementById('action-quick-user').addEventListener('click', () => {
        document.getElementById('user-modal-title').textContent = 'Register User';
        const idInput = document.getElementById('user-id');
        idInput.disabled = false;
        document.getElementById('user-modal-form').reset();
        openModal('user-modal');
    });

    document.getElementById('btn-open-add-book').addEventListener('click', () => {
        document.getElementById('book-modal-title').textContent = 'Add New Book';
        const isbnInput = document.getElementById('book-isbn');
        isbnInput.disabled = false;
        document.getElementById('book-modal-form').reset();
        openModal('book-modal');
    });

    document.getElementById('btn-open-add-user').addEventListener('click', () => {
        document.getElementById('user-modal-title').textContent = 'Register User';
        const idInput = document.getElementById('user-id');
        idInput.disabled = false;
        document.getElementById('user-modal-form').reset();
        openModal('user-modal');
    });

    // 4. Modal Submit Forms
    document.getElementById('book-modal-form').addEventListener('submit', handleBookFormSubmit);
    document.getElementById('user-modal-form').addEventListener('submit', handleUserFormSubmit);

    // 5. Transaction Handlers
    document.getElementById('borrow-form').addEventListener('submit', handleBorrowSubmit);
    document.getElementById('btn-action-return').addEventListener('click', handleReturnClick);
    document.getElementById('btn-action-renew').addEventListener('click', handleRenewClick);
    document.getElementById('btn-action-hold').addEventListener('click', handleHoldClick);
}

// Handler: Add/Edit Book Form Submit
function handleBookFormSubmit(e) {
    e.preventDefault();
    const isbn = document.getElementById('book-isbn').value;
    const title = document.getElementById('book-title').value;
    const author = document.getElementById('book-author').value;
    const genre = document.getElementById('book-genre').value;

    const isEdit = document.getElementById('book-isbn').disabled;
    const endpoint = isEdit ? '/books/update' : '/books';

    fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ isbn, title, author, genre })
    })
    .then(res => res.json())
    .then(res => {
        if (res.success) {
            showToast(isEdit ? 'Updated Book' : 'Added Book', res.message, 'success');
            closeModal('book-modal');
            refreshAllData();
        } else {
            showToast('Form Error', res.message, 'error');
        }
    })
    .catch(err => {
        console.error('Book operation failed:', err);
        showToast('API Error', 'Failed to connect to backend api.', 'error');
    });
}

// Handler: Add/Edit User Form Submit
function handleUserFormSubmit(e) {
    e.preventDefault();
    const userId = document.getElementById('user-id').value;
    const name = document.getElementById('user-name').value;
    const contact = document.getElementById('user-contact').value;

    const isEdit = document.getElementById('user-id').disabled;
    const endpoint = isEdit ? '/users/update' : '/users';

    fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, name, contact })
    })
    .then(res => res.json())
    .then(res => {
        if (res.success) {
            showToast(isEdit ? 'Updated User' : 'Registered User', res.message, 'success');
            closeModal('user-modal');
            refreshAllData();
        } else {
            showToast('Form Error', res.message, 'error');
        }
    })
    .catch(err => {
        console.error('User operation failed:', err);
        showToast('API Error', 'Failed to connect to backend api.', 'error');
    });
}

// Handler: Borrow Submission
function handleBorrowSubmit(e) {
    e.preventDefault();
    const userId = document.getElementById('borrow-user-select').value;
    const isbn = document.getElementById('borrow-book-select').value;

    fetch(`${API_BASE}/borrow`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, isbn })
    })
    .then(res => res.json())
    .then(res => {
        if (res.success) {
            showToast('Issued Book', res.message, 'success');
            // Reset selectors
            document.getElementById('borrow-form').reset();
            refreshAllData();
        } else {
            showToast('Borrow Refused', res.message, 'error');
        }
    })
    .catch(err => {
        console.error('Borrow failed:', err);
        showToast('API Error', 'Failed to send borrow request.', 'error');
    });
}

// Handler: Return Action
function handleReturnClick() {
    const select = document.getElementById('active-record-select');
    if (!select.value) {
        showToast('Selection Required', 'Please select an active borrow record first.', 'info');
        return;
    }

    const [userIdStr, isbn] = select.value.split('_');
    const userId = parseInt(userIdStr);

    fetch(`${API_BASE}/return`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, isbn })
    })
    .then(res => res.json())
    .then(res => {
        if (res.success) {
            let msg = res.message;
            if (res.lateFee > 0) {
                msg += ` Late fee processed: Rs. ${res.lateFee.toFixed(2)}`;
            }
            showToast('Returned Book', msg, 'success');
            select.value = '';
            refreshAllData();
        } else {
            showToast('Return Failed', res.message, 'error');
        }
    })
    .catch(err => {
        console.error('Return request failed:', err);
        showToast('API Error', 'Failed to send return request.', 'error');
    });
}

// Handler: Renew Action
function handleRenewClick() {
    const select = document.getElementById('active-record-select');
    if (!select.value) {
        showToast('Selection Required', 'Please select an active borrow record first.', 'info');
        return;
    }

    const [userIdStr, isbn] = select.value.split('_');
    const userId = parseInt(userIdStr);

    fetch(`${API_BASE}/renew`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, isbn })
    })
    .then(res => res.json())
    .then(res => {
        if (res.success) {
            showToast('Renewed Book', `${res.message}. New due date: ${res.newDueDate}`, 'success');
            refreshAllData();
        } else {
            showToast('Renewal Rejected', res.message, 'error');
        }
    })
    .catch(err => {
        console.error('Renewal request failed:', err);
        showToast('API Error', 'Failed to send renewal request.', 'error');
    });
}

// Handler: Place Hold Action
function handleHoldClick() {
    const select = document.getElementById('active-record-select');
    const holdUserSelect = document.getElementById('hold-user-select');

    if (!select.value) {
        showToast('Selection Required', 'Please select an active borrow record first.', 'info');
        return;
    }

    if (!holdUserSelect.value) {
        showToast('Selection Required', 'Please select a user to place the hold for.', 'info');
        return;
    }

    const [currentBorrowerId, isbn] = select.value.split('_');
    const userId = parseInt(holdUserSelect.value);

    fetch(`${API_BASE}/hold`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, isbn })
    })
    .then(res => res.json())
    .then(res => {
        if (res.success) {
            showToast('Hold Placed', res.message, 'success');
            holdUserSelect.value = '';
            refreshAllData();
        } else {
            showToast('Hold Rejected', res.message, 'error');
        }
    })
    .catch(err => {
        console.error('Hold request failed:', err);
        showToast('API Error', 'Failed to place hold.', 'error');
    });
}
