// Additional UI Functions
Object.assign(window.SafeCampus, {
    // Initialize UI components
    initUI() {
        // Add tag styles to CSS dynamically
        const style = document.createElement('style');
        style.textContent = `
            .tag {
                display: inline-block;
                padding: 4px 8px;
                border-radius: 4px;
                font-size: 12px;
                font-weight: 600;
                text-transform: uppercase;
            }
            .tag[data-category="accident"] { background: #ffeaa7; color: #d35400; }
            .tag[data-category="crime"] { background: #fab1a0; color: #d63031; }
            .tag[data-category="facility"] { background: #a29bfe; color: #6c5ce7; }
            .tag[data-category="other"] { background: #dfe6e9; color: #636e72; }
            .small { font-size: 0.85rem; color: #666; }
            code {
                background: #f8f9fa;
                padding: 2px 6px;
                border-radius: 4px;
                font-family: monospace;
                font-size: 0.9rem;
            }
            .empty-state {
                text-align: center;
                padding: 40px;
                color: #666;
            }
            .empty-state i {
                font-size: 48px;
                color: #ccc;
                margin-bottom: 16px;
            }
            .empty-state h4 {
                margin-bottom: 8px;
                color: #333;
            }
            .empty-state.error i {
                color: #dc3545;
            }
        `;
        document.head.appendChild(style);
    },

    // Add some sample data for demo (optional)
    addSampleData() {
        // This is just for demonstration - remove in production
        if (!localStorage.getItem('sampleDataAdded')) {
            console.log('Adding sample data for demo...');
            
            // Add sample locations
            const sampleLocations = [
                {
                    name: "Main Gate Security Post",
                    type: "security",
                    lat: 6.453456,
                    lng: 100.509789,
                    description: "24/7 security post at main entrance"
                },
                {
                    name: "Campus Clinic",
                    type: "clinic",
                    lat: 6.452123,
                    lng: 100.508456,
                    description: "Medical center for students and staff"
                },
                {
                    name: "Library Emergency Point",
                    type: "emergency",
                    lat: 6.451789,
                    lng: 100.510123,
                    description: "Emergency help point near library"
                }
            ];

            // Add sample news
            const sampleNews = [
                {
                    title: "Campus Safety Reminder",
                    content: "Please report any suspicious activities immediately using the SafeCampus app. Your safety is our priority.",
                    createdBy: "Campus Security"
                },
                {
                    title: "New Security Posts Installed",
                    content: "Three new security posts have been installed near the science building. Security personnel are available 24/7.",
                    createdBy: "Admin"
                }
            ];

            localStorage.setItem('sampleDataAdded', 'true');
        }
    }
});

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function() {
    SafeCampus.init();
    SafeCampus.initUI();
    SafeCampus.addSampleData();
});