// Authentication Functions
Object.assign(window.SafeCampus, {
    // Initialize the application
    init() {
        this.setupEventListeners();
        this.setupAuthListener();
        this.checkAutoLogin();
    },

    // Setup event listeners
    setupEventListeners() {
        // Login form
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.login();
            });
        }

        // Menu toggle
        const menuToggle = document.getElementById('menuToggle');
        if (menuToggle) {
            menuToggle.addEventListener('click', () => {
                document.getElementById('sidebar').classList.toggle('active');
            });
        }

        // User dropdown
        const userBtn = document.getElementById('userBtn');
        if (userBtn) {
            userBtn.addEventListener('click', () => {
                document.getElementById('userMenu').classList.toggle('hidden');
            });
        }

        // Close dropdowns when clicking outside
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.user-dropdown')) {
                document.getElementById('userMenu').classList.add('hidden');
            }
        });

        // Navigation links
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const section = link.getAttribute('data-section');
                this.showSection(section);
                
                // Update active state
                document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
                link.classList.add('active');
                
                // Close sidebar on mobile
                if (window.innerWidth < 992) {
                    document.getElementById('sidebar').classList.remove('active');
                }
            });
        });
    },

    // Setup Firebase auth state listener
    setupAuthListener() {
        firebase.onAuthStateChanged(this.auth, (user) => {
            if (user) {
                this.currentUser = user;
                this.showDashboard();
                this.updateUserInfo();
                this.loadDashboardData();
            } else {
                this.currentUser = null;
                this.showLogin();
            }
        });
    },

    // Check for auto-login (demo mode)
    checkAutoLogin() {
        const demoSession = localStorage.getItem('safeCampusDemoSession');
        if (demoSession === 'true') {
            // Auto-login with demo credentials
            this.demoLogin();
        }
    },

    // Demo login (for testing without real Firebase)
    async demoLogin() {
        this.currentUser = {
            uid: 'demo-admin',
            email: 'admin@safecampus.edu',
            displayName: 'Admin User'
        };
        
        localStorage.setItem('safeCampusDemoSession', 'true');
        this.showDashboard();
        this.updateUserInfo();
        this.showToast('Welcome', 'Logged in with demo credentials', 'success');
        
        // Load demo data
        this.loadDemoData();
    },

    // Real Firebase login
    async login() {
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const errorElement = document.getElementById('loginError');

        if (!email || !password) {
            this.showError('Please enter both email and password');
            return;
        }

        try {
            // For demo purposes, allow demo credentials
            if (email === 'admin@safecampus.edu' && password === 'Admin123!') {
                await this.demoLogin();
                return;
            }

            // Real Firebase authentication
            const userCredential = await firebase.signInWithEmailAndPassword(this.auth, email, password);
            this.currentUser = userCredential.user;
            
            // Clear any demo session
            localStorage.removeItem('safeCampusDemoSession');
            
            this.showToast('Success', 'Logged in successfully', 'success');
        } catch (error) {
            console.error('Login error:', error);
            
            // Show user-friendly error messages
            let errorMessage = 'Login failed. Please check your credentials.';
            switch (error.code) {
                case 'auth/user-not-found':
                    errorMessage = 'No account found with this email.';
                    break;
                case 'auth/wrong-password':
                    errorMessage = 'Incorrect password. Please try again.';
                    break;
                case 'auth/invalid-email':
                    errorMessage = 'Please enter a valid email address.';
                    break;
            }
            
            this.showError(errorMessage);
        }
    },

    // Logout
    async logout() {
        try {
            // Clear demo session
            localStorage.removeItem('safeCampusDemoSession');
            
            // Firebase logout if real user
            if (this.currentUser && this.currentUser.uid !== 'demo-admin') {
                await firebase.signOut(this.auth);
            }
            
            this.currentUser = null;
            this.showLogin();
            this.showToast('Logged Out', 'You have been logged out', 'info');
        } catch (error) {
            console.error('Logout error:', error);
            this.showToast('Error', 'Failed to logout', 'error');
        }
    },

    // Show error message
    showError(message) {
        const errorElement = document.getElementById('loginError');
        const errorText = document.getElementById('errorText');
        
        if (errorElement && errorText) {
            errorText.textContent = message;
            errorElement.classList.remove('hidden');
            
            // Auto-hide after 5 seconds
            setTimeout(() => {
                errorElement.classList.add('hidden');
            }, 5000);
        }
    },

    // Show toast notification
    showToast(title, message, type = 'info') {
        const toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
            <div>
                <strong>${title}</strong>
                <p>${message}</p>
            </div>
        `;

        toastContainer.appendChild(toast);

        // Auto-remove after 5 seconds
        setTimeout(() => {
            toast.remove();
        }, 5000);
    },

    // Update user info in UI
    updateUserInfo() {
        if (!this.currentUser) return;

        const userName = document.getElementById('userName');
        const userAvatar = document.getElementById('userAvatar');
        const headerUserName = document.getElementById('headerUserName');
        const headerUserAvatar = document.getElementById('headerUserAvatar');

        const name = this.currentUser.displayName || this.currentUser.email?.split('@')[0] || 'Admin';
        const initial = name.charAt(0).toUpperCase();

        if (userName) userName.textContent = name;
        if (userAvatar) userAvatar.textContent = initial;
        if (headerUserName) headerUserName.textContent = name;
        if (headerUserAvatar) headerUserAvatar.textContent = initial;
    },

    // Show login page
    showLogin() {
        document.getElementById('loginPage').classList.remove('hidden');
        document.getElementById('dashboardPage').classList.add('hidden');
    },

    // Show dashboard
    showDashboard() {
        document.getElementById('loginPage').classList.add('hidden');
        document.getElementById('dashboardPage').classList.remove('hidden');
        
        // Show dashboard section by default
        this.showSection('dashboard');
    },

    // Show section
    showSection(sectionId) {
        // Hide all sections
        document.querySelectorAll('.content-section').forEach(section => {
            section.classList.add('hidden');
        });

        // Show selected section
        const section = document.getElementById(sectionId);
        if (section) {
            section.classList.remove('hidden');
            
            // Load section data
            switch(sectionId) {
                case 'dashboard':
                    this.loadDashboardData();
                    break;
                case 'incidents':
                    this.loadIncidents();
                    break;
                case 'locations':
                    this.loadLocations();
                    break;
                case 'news':
                    this.loadNews();
                    break;
                case 'analytics':
                    this.loadAnalytics();
                    break;
            }
        }
    },

    // Close modal
    closeModal() {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.classList.add('hidden');
        });
    }
});