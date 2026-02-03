// Firestore Database Operations
window.SafeCampus = window.SafeCampus || {};

Object.assign(window.SafeCampus, {

  // ---------- UI helpers ----------
  closeModal() {
    const addLocationModal = document.getElementById("addLocationModal");
    if (addLocationModal) addLocationModal.classList.add("hidden");

    const addNewsModal = document.getElementById("addNewsModal");
    if (addNewsModal) addNewsModal.classList.add("hidden");
  },

  // ---------- Load incidents ----------
  async loadIncidents() {
    try {
      const tableBody = document.getElementById("incidentsTable");
      if (!tableBody) return;

      tableBody.innerHTML = `
        <tr>
          <td colspan="6" class="text-center">
            <div class="loading">
              <i class="fas fa-spinner fa-spin"></i>
              <p>Loading incident reports from Firebase...</p>
            </div>
          </td>
        </tr>
      `;

      const q = firebase.query(
        firebase.collection(this.db, this.collections.INCIDENTS),
        firebase.orderBy("reportedAt", "desc")
      );

      const snapshot = await firebase.getDocs(q);
      const incidents = [];

      snapshot.forEach((doc) => {
        incidents.push({ id: doc.id, ...doc.data() });
      });

      // badge
      const badge = document.getElementById("incidentsBadge");
      if (badge) badge.textContent = incidents.length;

      if (incidents.length === 0) {
        tableBody.innerHTML = `
          <tr>
            <td colspan="6" class="text-center">
              <i class="fas fa-inbox" style="font-size: 48px; color: #ccc; margin-bottom: 16px;"></i>
              <h4>No Incident Reports</h4>
              <p>No reports have been submitted from the mobile app yet.</p>
            </td>
          </tr>
        `;
        return;
      }

      let html = "";
      incidents.forEach((incident) => {
        const reportedDate = incident.reportedAt
          ? this.formatDateTime(incident.reportedAt.toDate())
          : "Unknown";

        const gps =
          incident.lat && incident.lng
            ? `${Number(incident.lat).toFixed(6)}, ${Number(incident.lng).toFixed(6)}`
            : "No GPS";

        html += `
          <tr>
            <td><strong>${incident.username || "Anonymous"}</strong></td>
            <td>${reportedDate}</td>
            <td><span class="tag">${incident.category || "other"}</span></td>
            <td>${incident.description || "No description"}</td>
            <td><code>${gps}</code></td>
            <td class="small">${(incident.userAgent || "Unknown device").substring(0, 30)}...</td>
          </tr>
        `;
      });

      tableBody.innerHTML = html;

      if (this.showToast) this.showToast("Success", `Loaded ${incidents.length} incident reports`, "success");

    } catch (error) {
      console.error("Error loading incidents:", error);

      const tableBody = document.getElementById("incidentsTable");
      if (tableBody) {
        tableBody.innerHTML = `
          <tr>
            <td colspan="6" class="text-center">
              <i class="fas fa-exclamation-triangle" style="color: #dc3545; font-size: 48px; margin-bottom: 16px;"></i>
              <h4>Error Loading Data</h4>
              <p>Failed to connect to Firebase database.</p>
              <p class="small">Error: ${error.message}</p>
              <button class="btn btn-secondary" onclick="SafeCampus.loadIncidents()">
                <i class="fas fa-sync-alt"></i>
                Try Again
              </button>
            </td>
          </tr>
        `;
      }

      if (this.showToast) this.showToast("Error", "Failed to load incident reports", "error");
    }
  },

  // ---------- Load locations ----------
  async loadLocations() {
    try {
      const container = document.getElementById("locationsGrid");
      if (!container) return;

      container.innerHTML = `
        <div class="loading">
          <i class="fas fa-spinner fa-spin"></i>
          <p>Loading campus locations...</p>
        </div>
      `;

      const snapshot = await firebase.getDocs(firebase.collection(this.db, this.collections.LOCATIONS));
      const locations = [];
      snapshot.forEach((doc) => locations.push({ id: doc.id, ...doc.data() }));

      const total = document.getElementById("totalLocations");
      if (total) total.textContent = locations.length;

      if (locations.length === 0) {
        container.innerHTML = `
          <div class="empty-state">
            <i class="fas fa-map-marker-alt" style="font-size: 48px; color: #ccc; margin-bottom: 16px;"></i>
            <h4>No Locations Added</h4>
            <p>No campus locations have been added yet.</p>
            <button class="btn btn-primary" onclick="SafeCampus.showAddLocationModal()">
              <i class="fas fa-plus"></i>
              Add First Location
            </button>
          </div>
        `;
        return;
      }

      let html = "";
      locations.forEach((loc) => {
        const type = loc.type || "other";

        const icon =
          type === "clinic" ? "fa-hospital" :
          type === "emergency" ? "fa-exclamation-triangle" :
          type === "security" ? "fa-shield-alt" :
          "fa-map-marker-alt";

        const typeLabel =
          type === "clinic" ? "Clinic" :
          type === "emergency" ? "Emergency Point" :
          type === "security" ? "Security Post" :
          "Other";

        const gps = (loc.lat != null && loc.lng != null)
          ? `${Number(loc.lat).toFixed(6)}, ${Number(loc.lng).toFixed(6)}`
          : `N/A`;

        html += `
          <div class="location-card">
            <div class="location-header">
              <i class="fas ${icon}"></i>
              <h4>${loc.name || "Location"}</h4>
            </div>
            <div class="location-body">
              <p><strong>Type:</strong> ${typeLabel}</p>
              <p><strong>GPS:</strong> ${gps}</p>
              ${loc.description ? `<p><strong>Description:</strong> ${loc.description}</p>` : ""}
              <p class="small"><strong>Added:</strong> ${loc.createdAt ? this.formatDateTime(loc.createdAt.toDate()) : "Unknown"}</p>
            </div>
          </div>
        `;
      });

      container.innerHTML = html;

    } catch (error) {
      console.error("Error loading locations:", error);
      const container = document.getElementById("locationsGrid");
      if (container) {
        container.innerHTML = `
          <div class="empty-state error">
            <i class="fas fa-exclamation-triangle"></i>
            <p>Failed to load locations: ${error.message}</p>
          </div>
        `;
      }
    }
  },

  // ---------- Load news ----------
  async loadNews() {
    try {
      const container = document.getElementById("newsList");
      if (!container) return;

      container.innerHTML = `
        <div class="loading">
          <i class="fas fa-spinner fa-spin"></i>
          <p>Loading news updates...</p>
        </div>
      `;

      const q = firebase.query(
        firebase.collection(this.db, this.collections.NEWS),
        firebase.orderBy("createdAt", "desc")
      );

      const snapshot = await firebase.getDocs(q);
      const newsItems = [];
      snapshot.forEach((doc) => newsItems.push({ id: doc.id, ...doc.data() }));

      const total = document.getElementById("totalNews");
      if (total) total.textContent = newsItems.length;

      if (newsItems.length === 0) {
        container.innerHTML = `
          <div class="empty-state">
            <i class="fas fa-newspaper" style="font-size: 48px; color: #ccc; margin-bottom: 16px;"></i>
            <h4>No News Updates</h4>
            <p>No news has been posted yet.</p>
            <button class="btn btn-primary" onclick="SafeCampus.showAddNewsModal()">
              <i class="fas fa-plus"></i>
              Post First News
            </button>
          </div>
        `;
        return;
      }

      let html = "";
      newsItems.forEach((news) => {
        html += `
          <div class="news-item">
            <div class="news-header">
              <div class="news-title">${news.title || "Untitled"}</div>
              <div class="news-date">${news.createdAt ? this.formatDateTime(news.createdAt.toDate()) : "Unknown date"}</div>
            </div>
            <div class="news-content">${news.content || ""}</div>
            <div class="news-footer">
              <small>Posted by: ${news.createdBy || "Admin"}</small>
            </div>
          </div>
        `;
      });

      container.innerHTML = html;

    } catch (error) {
      console.error("Error loading news:", error);
      const container = document.getElementById("newsList");
      if (container) container.innerHTML = `<div class="alert alert-error">Error loading news: ${error.message}</div>`;
    }
  },

  // ---------- Add new location ----------
  async saveLocation() {
    try {
      const name = document.getElementById("locationName").value.trim();
      const type = document.getElementById("locationType").value; // security/clinic/emergency/other
      const lat = parseFloat(document.getElementById("latitude").value);
      const lng = parseFloat(document.getElementById("longitude").value);
      const description = document.getElementById("locationDesc").value.trim();

      if (!name || !type || Number.isNaN(lat) || Number.isNaN(lng)) {
        if (this.showToast) this.showToast("Error", "Please fill all required fields with valid data", "error");
        else alert("Please fill all required fields with valid data");
        return;
      }

      const locationData = {
        name,
        type,
        lat,
        lng,
        description: description || "",
        createdAt: firebase.serverTimestamp()
      };

      // IMPORTANT: write to "locations"
      await firebase.addDoc(firebase.collection(this.db, this.collections.LOCATIONS), locationData);

      this.closeModal();
      if (this.showToast) this.showToast("Success", "Location added successfully", "success");

      // Refresh list
      this.loadLocations();

      // Clear form
      const form = document.getElementById("locationForm");
      if (form) form.reset();

    } catch (error) {
      console.error("Error adding location:", error);
      if (this.showToast) this.showToast("Error", `Failed to add location: ${error.message}`, "error");
      else alert("Failed to add location: " + error.message);
    }
  },

  // ---------- Add news ----------
  async saveNews() {
    try {
      const title = document.getElementById("newsTitle").value.trim();
      const content = document.getElementById("newsContent").value.trim();

      if (!title || !content) {
        if (this.showToast) this.showToast("Error", "Please fill both title and content", "error");
        else alert("Please fill both title and content");
        return;
      }

      const newsData = {
        title,
        content,
        createdAt: firebase.serverTimestamp(),
        createdBy: (this.currentUser && (this.currentUser.displayName || this.currentUser.email)) || "Admin"
      };

      await firebase.addDoc(firebase.collection(this.db, this.collections.NEWS), newsData);

      this.closeModal();
      if (this.showToast) this.showToast("Success", "News posted successfully", "success");

      this.loadNews();

      const form = document.getElementById("newsForm");
      if (form) form.reset();

    } catch (error) {
      console.error("Error adding news:", error);
      if (this.showToast) this.showToast("Error", `Failed to post news: ${error.message}`, "error");
      else alert("Failed to post news: " + error.message);
    }
  },

  // ---------- Format date and time ----------
  formatDateTime(date) {
    if (!date) return "Unknown";
    return date.toLocaleDateString("en-MY", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  },

  // ---------- Modal open helpers ----------
  showAddLocationModal() {
    const el = document.getElementById("addLocationModal");
    if (el) el.classList.remove("hidden");
  },

  showAddNewsModal() {
    const el = document.getElementById("addNewsModal");
    if (el) el.classList.remove("hidden");
  }
});
