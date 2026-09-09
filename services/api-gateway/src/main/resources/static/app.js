/**
 * CraveBite - Chennai & Taramani Multi-Role Platform Application
 * Features:
 * 1. Customer: Location Detection & Search, Food Catalog with Stock Status, Cart, UPI/Card/COD Checkout, Live Order Tracking
 * 2. Restaurant Kitchen: Incoming Orders Queue (Accept/Reject/Ready), Menu Stock Toggle (In-Stock/Out-of-Stock), Add Dish
 * 3. Delivery Partner: Online/Offline Availability Toggle, Available Pickups Feed, Accept Task, Step Progression (Picked Up -> Delivered)
 * 4. Admin Console: Platform KPIs, Live Orders Ledger, Fleet GPS Radar, Microservices Telemetry
 */

const App = {
    state: {
        activeRole: 'customer',
        user: { username: "customer1", fullName: "Alex Customer", role: "ROLE_CUSTOMER" },
        token: "mock_jwt_token_chennai",
        location: {
            address: "Taramani - Ramanujan IT City, Chennai 600113",
            latitude: 12.9863,
            longitude: 80.2432,
            isServiceable: true,
            nearestPartnerDistanceKm: 0.5,
            estimatedDeliveryMinutes: 25
        },
        isViewOnly: false,
        cart: [],
        currentCategory: 'ALL',
        searchQuery: '',
        sortBy: 'popular',
        selectedPaymentMethod: 'UPI',
        deliveryOnline: true,
        todayEarnings: 640,
        completedDeliveries: 6,
        activeDeliveryTask: null,

        // Live Food Catalog (Dynamic Availability & Stock)
        catalog: [
            {
                id: 1,
                name: "Chennai Ghee Podi Crispy Dosa",
                category: "South Indian",
                cuisine: "South Indian",
                restaurant: "A2B - Adyar Ananda Bhavan (Taramani)",
                restaurantId: 1,
                price: 140,
                rating: 4.9,
                emoji: "🥞",
                inStock: true,
                description: "Crispy golden fermented crepe roasted in pure desi ghee, dusted with spicy gun powder (podi), served with 3 chutneys & sambar."
            },
            {
                id: 2,
                name: "Dindigul Thalappakatti Mutton Biryani",
                category: "Biryani",
                cuisine: "South Indian",
                restaurant: "Thalappakatti Biryani (Velachery)",
                restaurantId: 2,
                price: 360,
                rating: 4.9,
                emoji: "🍛",
                inStock: true,
                description: "Authentic Seeraga Samba rice cooked with tender mutton chunks and traditional handmade masala, served with brinjal gravy & raita."
            },
            {
                id: 3,
                name: "Chettinad Spicy Pepper Chicken Gravy",
                category: "Chettinad",
                cuisine: "South Indian",
                restaurant: "Anjappar Chettinad (Taramani OMR)",
                restaurantId: 3,
                price: 290,
                rating: 4.8,
                emoji: "🍗",
                inStock: true,
                description: "Country chicken cooked in fresh stone-ground black pepper, shallots, curry leaves, and roasted spices."
            },
            {
                id: 4,
                name: "Paneer Butter Masala & Butter Naan",
                category: "North Indian",
                cuisine: "North Indian",
                restaurant: "Sangeetha Veg Restaurant (OMR)",
                restaurantId: 4,
                price: 240,
                rating: 4.7,
                emoji: "🥘",
                inStock: true,
                description: "Soft cottage cheese simmered in rich creamy tomato cashew gravy, served with hot clay-oven butter naan."
            },
            {
                id: 5,
                name: "Authentic Madras Filter Coffee & Medu Vada",
                category: "Snacks",
                cuisine: "South Indian",
                restaurant: "Madras Coffee House (Taramani)",
                restaurantId: 5,
                price: 95,
                rating: 4.9,
                emoji: "☕",
                inStock: true,
                description: "Freshly brewed chicory coffee in brass dabara set, paired with two golden crispy lentil vadas and coconut chutney."
            },
            {
                id: 6,
                name: "Taramani Smoky Chicken Shawarma Roll",
                category: "Street Food",
                cuisine: "Arabian",
                restaurant: "Arabian Nights (Kandanchavadi)",
                restaurantId: 6,
                price: 160,
                rating: 4.8,
                emoji: "🌯",
                inStock: true,
                description: "Charcoal-grilled spiced shredded chicken rolled in warm rumali roti with garlic toum, pickled veggies, and tahini."
            },
            {
                id: 7,
                name: "Margherita Truffle Pizza",
                category: "Pizza",
                cuisine: "Italian",
                restaurant: "Toscano (Phoenix Marketcity)",
                restaurantId: 7,
                price: 450,
                rating: 4.8,
                emoji: "🍕",
                inStock: true,
                description: "Stone-baked sourdough crust topped with San Marzano marinara, fresh mozzarella bocconcini, basil, and aromatic truffle oil."
            },
            {
                id: 8,
                name: "Double Smash Cheesy Burger & Peri-Peri Fries",
                category: "Burgers",
                cuisine: "Continental",
                restaurant: "Burger Lounge (Thiruvanmiyur)",
                restaurantId: 8,
                price: 260,
                rating: 4.7,
                emoji: "🍔",
                inStock: true,
                description: "Double seasoned smashed patties with melted American cheddar, caramelized onions, and signature house sauce."
            }
        ],

        // Unified Live Orders Store
        orders: [
            {
                id: 101,
                orderNumber: "ORD-CHN-1001",
                customerName: "Alex Customer",
                restaurantName: "A2B - Adyar Ananda Bhavan (Taramani)",
                restaurantId: 1,
                deliveryAddress: "Ramanujan IT City, TRIL Infopark, Taramani, Chennai",
                items: [
                    { name: "Chennai Ghee Podi Crispy Dosa", quantity: 2, price: 140 },
                    { name: "Authentic Madras Filter Coffee & Medu Vada", quantity: 1, price: 95 }
                ],
                totalAmount: 375,
                paymentMethod: "UPI",
                paymentStatus: "PAID",
                status: "PENDING_RESTAURANT_ACCEPTANCE",
                placedAt: "Just now",
                courierName: null
            },
            {
                id: 102,
                orderNumber: "ORD-CHN-1002",
                customerName: "Priya Sundaram",
                restaurantName: "Anjappar Chettinad (Taramani OMR)",
                restaurantId: 3,
                deliveryAddress: "Ascendas Tech Park, CSIR Road, Taramani, Chennai",
                items: [
                    { name: "Chettinad Spicy Pepper Chicken Gravy", quantity: 1, price: 290 }
                ],
                totalAmount: 290,
                paymentMethod: "CARD",
                paymentStatus: "PAID",
                status: "PREPARING",
                placedAt: "5 mins ago",
                courierName: null
            },
            {
                id: 103,
                orderNumber: "ORD-CHN-1003",
                customerName: "Venkatesh K.",
                restaurantName: "Thalappakatti Biryani (Velachery)",
                restaurantId: 2,
                deliveryAddress: "142 Velachery Main Road, Chennai",
                items: [
                    { name: "Dindigul Thalappakatti Mutton Biryani", quantity: 2, price: 360 }
                ],
                totalAmount: 720,
                paymentMethod: "UPI",
                paymentStatus: "PAID",
                status: "READY_FOR_PICKUP",
                placedAt: "12 mins ago",
                courierName: null
            }
        ],

        // Delivery Fleet
        couriers: [
            { id: 1, name: "Murugan S. (You)", vehicle: "Hero Splendor (TN-07-BW-4821)", phone: "+91 98401 22334", online: true, zone: "Taramani Hub", rating: "4.95 ★" },
            { id: 2, name: "Karthik Raja", vehicle: "Honda Activa (TN-09-AX-9912)", phone: "+91 98402 33445", online: true, zone: "Ascendas IT Park", rating: "4.90 ★" },
            { id: 3, name: "Saravanan P.", vehicle: "TVS Apache (TN-22-CZ-1144)", phone: "+91 98403 44556", online: true, zone: "Velachery Main Rd", rating: "4.85 ★" },
            { id: 4, name: "Dinesh Kumar", vehicle: "Bajaj Pulsar (TN-07-DK-7788)", phone: "+91 98404 55667", online: false, zone: "Adyar Depot", rating: "4.88 ★" }
        ]
    },

    // Curated Chennai & Taramani Sample Locations
    sampleLocations: [
        {
            name: "Taramani - Ramanujan IT City",
            desc: "TRIL Infopark, Rajiv Gandhi Salai, Taramani, Chennai 600113",
            lat: 12.9863,
            lng: 80.2432,
            serviceable: true,
            distance: 0.5
        },
        {
            name: "Taramani - Ascendas IT Park & CSIR Road",
            desc: "Ascendas Tech Park, Taramani, Chennai 600113",
            lat: 12.9892,
            lng: 80.2475,
            serviceable: true,
            distance: 0.9
        },
        {
            name: "Velachery Main Road & Phoenix Marketcity",
            desc: "142 Velachery Road, Indira Gandhi Nagar, Chennai 600042",
            lat: 12.9759,
            lng: 80.2212,
            serviceable: true,
            distance: 2.8
        },
        {
            name: "Adyar - LB Road & Besant Nagar",
            desc: "Lattice Bridge Rd, Adyar, Chennai 600020",
            lat: 13.0012,
            lng: 80.2565,
            serviceable: true,
            distance: 3.4
        },
        {
            name: "Thiruvanmiyur Beach Road & OMR Junction",
            desc: "East Coast Road / OMR Junction, Thiruvanmiyur, Chennai 600041",
            lat: 12.9830,
            lng: 80.2594,
            serviceable: true,
            distance: 2.1
        },
        {
            name: "Perungudi & Kandanchavadi OMR Tech Zone",
            desc: "Rajiv Gandhi Salai, Perungudi, Chennai 600096",
            lat: 12.9654,
            lng: 80.2461,
            serviceable: true,
            distance: 2.4
        },
        {
            name: "Mahabalipuram Coastal Village, ECR",
            desc: "Shore Temple Road, Mahabalipuram, Tamil Nadu 603104",
            lat: 12.6208,
            lng: 80.1983,
            serviceable: false,
            distance: 42.0
        },
        {
            name: "Kanchipuram Temple Town, Tamil Nadu",
            desc: "Gandhi Road, Kanchipuram, Tamil Nadu 631501",
            lat: 12.8342,
            lng: 79.7036,
            serviceable: false,
            distance: 68.5
        },
        {
            name: "Yelagiri Hill Station, Tamil Nadu",
            desc: "Athanavur, Yelagiri Hills, Tamil Nadu 635853",
            lat: 12.5786,
            lng: 78.6399,
            serviceable: false,
            distance: 215.0
        }
    ],

    init() {
        console.log("Initializing CraveBite Chennai & Taramani Multi-Role Application...");

        // Load saved state if any
        const savedLoc = localStorage.getItem("cravebite_location");
        if (savedLoc) {
            try {
                this.state.location = JSON.parse(savedLoc);
            } catch (e) {}
        }

        this.updateLocationHeaderDisplay();
        this.renderCatalog();
        this.renderCustomerRecentOrders();
        this.updateNotificationBadges();
        this.renderSuggestions(this.sampleLocations);
    },

    // ==========================================
    // 1. Role Switching Engine
    // ==========================================
    switchRole(role) {
        this.state.activeRole = role;

        // Update tab styling
        document.querySelectorAll(".role-tab").forEach(tab => tab.classList.remove("active"));
        const activeTab = document.getElementById(`tab-role-${role}`);
        if (activeTab) activeTab.classList.add("active");

        // Hide all views
        document.getElementById("customer-view-wrapper").classList.add("hidden");
        document.getElementById("restaurant-view-wrapper").classList.add("hidden");
        document.getElementById("delivery-view-wrapper").classList.add("hidden");
        document.getElementById("admin-view-wrapper").classList.add("hidden");

        // Customer header only on customer view
        document.getElementById("customer-header").classList.toggle("hidden", role !== 'customer');

        const personaDisplay = document.getElementById("current-persona-name");

        if (role === 'customer') {
            document.getElementById("customer-view-wrapper").classList.remove("hidden");
            personaDisplay.textContent = "Customer (Alex)";
            this.state.user = { username: "customer1", fullName: "Alex Customer", role: "ROLE_CUSTOMER" };
            this.renderCatalog();
            this.renderCustomerRecentOrders();
        } else if (role === 'restaurant') {
            document.getElementById("restaurant-view-wrapper").classList.remove("hidden");
            personaDisplay.textContent = "Kitchen Manager (restaurant1)";
            this.state.user = { username: "restaurant1", fullName: "A2B Kitchen Head", role: "ROLE_RESTAURANT" };
            this.refreshRestaurantData();
        } else if (role === 'delivery') {
            document.getElementById("delivery-view-wrapper").classList.remove("hidden");
            personaDisplay.textContent = "Courier: Murugan (delivery1)";
            this.state.user = { username: "delivery1", fullName: "Murugan S.", role: "ROLE_DELIVERY" };
            this.refreshDeliveryTasks();
        } else if (role === 'admin') {
            document.getElementById("admin-view-wrapper").classList.remove("hidden");
            personaDisplay.textContent = "Super Admin (admin)";
            this.state.user = { username: "admin", fullName: "Platform Admin", role: "ROLE_ADMIN" };
            this.refreshAdminData();
        }

        this.updateNotificationBadges();
    },

    openPersonaModal() {
        document.getElementById("persona-modal").classList.remove("hidden");
    },

    closePersonaModal() {
        document.getElementById("persona-modal").classList.add("hidden");
    },

    selectPersona(role) {
        this.closePersonaModal();
        this.switchRole(role);
        this.showToast(`Switched persona to ${role.toUpperCase()} mode! 🔄`, "info");
    },

    updateNotificationBadges() {
        // Pending restaurant orders
        const pendingCount = this.state.orders.filter(o => o.status === 'PENDING_RESTAURANT_ACCEPTANCE').length;
        const restBadge = document.getElementById("badge-restaurant-orders");
        if (restBadge) {
            restBadge.textContent = pendingCount;
            restBadge.classList.toggle("hidden", pendingCount === 0);
        }

        // Available deliveries
        const deliveryCount = this.state.orders.filter(o => o.status === 'READY_FOR_PICKUP' || o.status === 'PREPARING').length;
        const delBadge = document.getElementById("badge-delivery-tasks");
        if (delBadge) {
            delBadge.textContent = deliveryCount;
            delBadge.classList.toggle("hidden", deliveryCount === 0);
        }
    },

    // ==========================================
    // 2. Customer View & Location Gatekeeper
    // ==========================================
    updateLocationHeaderDisplay() {
        const loc = this.state.location;
        const addr = loc ? loc.address : "Taramani - Ramanujan IT City, Chennai";
        const shortName = addr.split("-")[0].split(",")[0].trim();
        
        const headerAddr = document.getElementById("header-address-text");
        if (headerAddr) headerAddr.textContent = addr;

        const heroAddr = document.getElementById("hero-address-highlight");
        if (heroAddr) heroAddr.textContent = shortName;

        const eta = loc && loc.estimatedDeliveryMinutes ? `${loc.estimatedDeliveryMinutes} mins` : "25 mins";
        const headerEta = document.getElementById("header-eta-text");
        if (headerEta) headerEta.textContent = `~${eta}`;

        const heroEta = document.getElementById("hero-eta-display");
        if (heroEta) heroEta.textContent = `~${eta}`;
    },

    openLocationSelector() {
        document.getElementById("location-modal-overlay").classList.remove("hidden");
        document.getElementById("address-search-input").value = "";
        document.getElementById("serviceability-spinner").classList.add("hidden");
        this.renderSuggestions(this.sampleLocations);
    },

    closeLocationSelector() {
        document.getElementById("location-modal-overlay").classList.add("hidden");
    },

    detectCurrentLocation() {
        if (!navigator.geolocation) {
            this.showToast("Geolocation is not supported by your browser.", "error");
            return;
        }

        const btn = document.getElementById("btn-gps-detect");
        btn.style.opacity = "0.7";
        this.showLoader(true, "Detecting GPS coordinates (Chennai)...");

        navigator.geolocation.getCurrentPosition(
            async (pos) => {
                btn.style.opacity = "1";
                const lat = pos.coords.latitude;
                const lng = pos.coords.longitude;

                // Haversine distance from Taramani Hub (12.9863, 80.2432)
                const taramaniLat = 12.9863;
                const taramaniLng = 80.2432;
                const dLat = (lat - taramaniLat) * Math.PI / 180;
                const dLon = (lng - taramaniLng) * Math.PI / 180;
                const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                          Math.cos(taramaniLat * Math.PI / 180) * Math.cos(lat * Math.PI / 180) *
                          Math.sin(dLon/2) * Math.sin(dLon/2);
                const distanceKm = Math.round(6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));

                // If browser IP locates far away (common on Indian desktop broadband)
                if (distanceKm > 25) {
                    this.showLoader(false);
                    const useTaramani = confirm(
                        `Your desktop network IP reported coordinates (${lat.toFixed(2)}, ${lng.toFixed(2)}) which is ~${distanceKm} km away (routed through a regional telecom ISP gateway).\n\nWould you like to snap your delivery address to Taramani, Chennai (Ramanujan IT City)?`
                    );

                    if (useTaramani) {
                        await this.verifyServiceability(taramaniLat, taramaniLng, "Taramani - Ramanujan IT City, Chennai 600113");
                        return;
                    }
                }

                const addressStr = `Current GPS Location (${lat.toFixed(4)}, ${lng.toFixed(4)})`;
                await this.verifyServiceability(lat, lng, addressStr);
            },
            (error) => {
                btn.style.opacity = "1";
                this.showLoader(false);
                console.warn("GPS error:", error);
                this.verifyServiceability(12.9863, 80.2432, "Taramani - Ramanujan IT City, Chennai 600113");
            },
            { timeout: 8000, enableHighAccuracy: true }
        );
    },

    onAddressSearch(query) {
        const q = query.trim().toLowerCase();
        const clearBtn = document.getElementById("clear-search-btn");
        clearBtn.classList.toggle("hidden", !q);

        if (!q) {
            this.renderSuggestions(this.sampleLocations);
            return;
        }

        const filtered = this.sampleLocations.filter(loc =>
            loc.name.toLowerCase().includes(q) || loc.desc.toLowerCase().includes(q)
        );

        if (filtered.length === 0) {
            const isOutOfZone = /kanchi|yelagiri|ooty|madurai|salem|remote|village|hill|pondicherry|delhi|mumbai|bangalore/i.test(q);
            const dynamicLoc = {
                name: query,
                desc: `Custom search address: ${query}, Tamil Nadu`,
                lat: isOutOfZone ? 12.5000 : 12.9850,
                lng: isOutOfZone ? 78.5000 : 80.2400,
                serviceable: !isOutOfZone,
                distance: isOutOfZone ? 85.0 : 1.8
            };
            this.renderSuggestions([dynamicLoc]);
        } else {
            this.renderSuggestions(filtered);
        }
    },

    clearAddressSearch() {
        document.getElementById("address-search-input").value = "";
        document.getElementById("clear-search-btn").classList.add("hidden");
        this.renderSuggestions(this.sampleLocations);
    },

    renderSuggestions(locations) {
        const list = document.getElementById("location-suggestions-list");
        if (!list) return;
        list.innerHTML = "";

        locations.forEach(loc => {
            const item = document.createElement("div");
            item.className = "suggestion-item";
            item.innerHTML = `
                <span class="item-icon">${loc.serviceable ? '📍' : '⚠️'}</span>
                <div class="item-info">
                    <span class="item-name">${loc.name}</span>
                    <span class="item-desc">${loc.desc} (~${loc.distance} km)</span>
                </div>
                <span class="badge-zone ${loc.serviceable ? 'in-zone' : 'out-zone'}">
                    ${loc.serviceable ? 'DELIVERY AVAILABLE' : 'OUT OF ZONE'}
                </span>
            `;
            item.onclick = () => this.verifyServiceability(loc.lat, loc.lng, loc.name);
            list.appendChild(item);
        });
    },

    async verifyServiceability(lat, lng, address) {
        this.showLoader(true, "Verifying delivery coverage in Chennai & Taramani...");

        try {
            const url = `/api/deliveries/serviceability?latitude=${lat}&longitude=${lng}&address=${encodeURIComponent(address)}`;
            const response = await fetch(url);

            if (response.ok) {
                const data = await response.json();
                this.handleServiceabilityResult(data);
            } else {
                this.fallbackHaversineCheck(lat, lng, address);
            }
        } catch (err) {
            console.warn("Delivery serviceability endpoint unavailable, executing client-side Haversine verification:", err);
            this.fallbackHaversineCheck(lat, lng, address);
        }
    },

    fallbackHaversineCheck(lat, lng, address) {
        const hubLat = 12.9863;
        const hubLng = 80.2432;
        const R = 6371;
        const dLat = (lat - hubLat) * Math.PI / 180;
        const dLon = (lng - hubLng) * Math.PI / 180;
        const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                  Math.cos(hubLat * Math.PI / 180) * Math.cos(lat * Math.PI / 180) *
                  Math.sin(dLon/2) * Math.sin(dLon/2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        const distance = Math.round((R * c) * 10) / 10;
        const isServiceable = distance <= 15.0;

        const result = {
            isServiceable: isServiceable,
            latitude: lat,
            longitude: lng,
            address: address,
            availableDeliveryPartners: isServiceable ? 6 : 0,
            nearestPartnerDistanceKm: distance,
            estimatedDeliveryMinutes: isServiceable ? Math.max(15, Math.round(distance * 3.5 + 15)) : null,
            message: isServiceable 
                ? `Delivery available in Chennai (${distance} km from Taramani Hub)` 
                : `Delivery not available at this address (${distance} km away, max coverage is 15 km)`
        };

        setTimeout(() => {
            this.handleServiceabilityResult(result);
        }, 400);
    },

    handleServiceabilityResult(data) {
        this.showLoader(false);
        this.closeLocationSelector();

        this.state.location = data;
        localStorage.setItem("cravebite_location", JSON.stringify(data));

        if (data.isServiceable) {
            this.state.isViewOnly = false;
            document.getElementById("out-of-service-view").classList.add("hidden");
            document.getElementById("home-view").classList.remove("hidden");
            document.getElementById("view-only-warning-banner").classList.add("hidden");
            document.getElementById("delivery-status-indicator").classList.remove("hidden");
            this.updateLocationHeaderDisplay();
            this.renderCatalog();
            this.showToast(`📍 Delivery confirmed for ${data.address || 'Taramani, Chennai'} (~${data.estimatedDeliveryMinutes || 25} mins).`, "success");
        } else {
            this.showOutOfServiceView(data);
        }
    },

    showOutOfServiceView(loc) {
        document.getElementById("home-view").classList.add("hidden");
        document.getElementById("out-of-service-view").classList.remove("hidden");
        document.getElementById("view-only-warning-banner").classList.remove("hidden");
        document.getElementById("delivery-status-indicator").classList.add("hidden");

        const addressText = loc ? (loc.address || "Selected Location") : "Selected Location";
        document.getElementById("header-address-text").textContent = addressText;
        document.getElementById("out-address-display").textContent = `"${addressText}"`;
        document.getElementById("out-distance-display").textContent = `${loc.nearestPartnerDistanceKm || 45.0} km`;
    },

    enterViewOnlyMode() {
        this.state.isViewOnly = true;
        document.getElementById("out-of-service-view").classList.add("hidden");
        document.getElementById("home-view").classList.remove("hidden");
        document.getElementById("view-only-warning-banner").classList.remove("hidden");
        this.renderCatalog();
        this.showToast("Entered View-Only Mode. Note: Ordering is disabled outside Chennai zone.", "info");
    },

    handleNotifyMe(e) {
        e.preventDefault();
        const email = document.getElementById("notify-email").value;
        this.showToast(`Thank you! We will alert ${email} when CraveBite expands to your location! 🚀`, "success");
        document.getElementById("notify-email").value = "";
    },

    showLoader(show, text = "Checking...") {
        const loader = document.getElementById("serviceability-spinner");
        if (show) {
            loader.querySelector(".loader-title").textContent = text;
            loader.classList.remove("hidden");
        } else {
            loader.classList.add("hidden");
        }
    },

    // ==========================================
    // 3. Customer Food Catalog & Cart Operations
    // ==========================================
    filterByCategory(category) {
        this.state.currentCategory = category;
        document.querySelectorAll("#category-chips-container .chip").forEach(chip => {
            chip.classList.toggle("active", chip.textContent.includes(category) || (category === 'ALL' && chip.textContent.includes("All")));
        });
        this.renderCatalog();
    },

    onSearchInput(val) {
        this.state.searchQuery = val.trim().toLowerCase();
        this.renderCatalog();
    },

    onSortChange(val) {
        this.state.sortBy = val;
        this.renderCatalog();
    },

    renderCatalog() {
        const grid = document.getElementById("food-items-grid");
        if (!grid) return;

        let items = [...this.state.catalog];

        if (this.state.currentCategory !== 'ALL') {
            items = items.filter(i => i.category === this.state.currentCategory);
        }

        if (this.state.searchQuery) {
            const q = this.state.searchQuery;
            items = items.filter(i => 
                i.name.toLowerCase().includes(q) ||
                i.restaurant.toLowerCase().includes(q) ||
                i.cuisine.toLowerCase().includes(q) ||
                i.description.toLowerCase().includes(q)
            );
        }

        if (this.state.sortBy === 'price-asc') {
            items.sort((a, b) => a.price - b.price);
        } else if (this.state.sortBy === 'price-desc') {
            items.sort((a, b) => b.price - a.price);
        } else {
            items.sort((a, b) => b.rating - a.rating);
        }

        grid.innerHTML = "";

        if (items.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: var(--text-muted);">
                    <p style="font-size: 2.5rem; margin-bottom: 0.5rem;">🔍</p>
                    <p style="font-size: 1.1rem; font-weight: 700;">No dishes found matching "${this.state.searchQuery}".</p>
                    <small>Try searching for Dosa, Biryani, Coffee, or Chicken!</small>
                </div>
            `;
            return;
        }

        items.forEach(item => {
            const card = document.createElement("div");
            card.className = "food-card";
            
            const isAvailable = item.inStock;
            const btnHtml = isAvailable 
                ? `<button class="btn-add-cart" onclick="App.addToCart(${item.id})">${this.state.isViewOnly ? 'View Details' : '+ Add to Order'}</button>`
                : `<button class="btn-add-cart out-of-stock" disabled>⛔ Out of Stock</button>`;

            card.innerHTML = `
                <div class="card-image-box">
                    <span class="card-tag">${item.cuisine}</span>
                    <span class="card-rating">★ ${item.rating}</span>
                    ${item.emoji}
                </div>
                <div class="card-body">
                    <span class="card-restaurant">${item.restaurant}</span>
                    <h4 class="card-title">${item.name}</h4>
                    <p class="card-desc">${item.description}</p>
                    <div class="card-footer">
                        <span class="card-price">₹${item.price.toFixed(0)}</span>
                        ${btnHtml}
                    </div>
                </div>
            `;
            grid.appendChild(card);
        });
    },

    addToCart(itemId) {
        if (this.state.isViewOnly) {
            this.showToast("⚠️ Ordering is disabled in View-Only mode for locations outside Chennai radius.", "error");
            return;
        }

        const item = this.state.catalog.find(i => i.id === itemId);
        if (!item) return;

        if (!item.inStock) {
            this.showToast(`"${item.name}" is currently Out of Stock at the kitchen.`, "error");
            return;
        }

        const existing = this.state.cart.find(i => i.id === itemId);
        if (existing) {
            existing.quantity += 1;
        } else {
            this.state.cart.push({ ...item, quantity: 1 });
        }

        this.updateCartUI();
        this.showToast(`Added "${item.name}" to cart! 🛍️`, "success");
    },

    updateCartUI() {
        const countBadge = document.getElementById("cart-badge-count");
        const container = document.getElementById("cart-items-container");
        const totalCount = this.state.cart.reduce((sum, item) => sum + item.quantity, 0);
        if (countBadge) countBadge.textContent = totalCount;

        if (!container) return;

        if (this.state.cart.length === 0) {
            container.innerHTML = `
                <div class="empty-cart-state">
                    <span class="empty-icon">🛒</span>
                    <p>Your cart is empty.</p>
                    <small>Add delicious Chennai delicacies from the menu to start!</small>
                </div>
            `;
            document.getElementById("cart-subtotal").textContent = "₹0";
            document.getElementById("cart-total-amount").textContent = "₹0";
            return;
        }

        let subtotal = 0;
        container.innerHTML = "";

        this.state.cart.forEach(item => {
            const itemTotal = item.price * item.quantity;
            subtotal += itemTotal;

            const row = document.createElement("div");
            row.className = "cart-item-row";
            row.innerHTML = `
                <div>
                    <div class="cart-item-title">${item.name}</div>
                    <div class="cart-item-price">₹${item.price.toFixed(0)} each</div>
                </div>
                <div class="cart-qty-ctrls">
                    <button onclick="App.modifyCartQty(${item.id}, -1)">−</button>
                    <span>${item.quantity}</span>
                    <button onclick="App.modifyCartQty(${item.id}, 1)">+</button>
                </div>
            `;
            container.appendChild(row);
        });

        document.getElementById("cart-subtotal").textContent = `₹${subtotal.toFixed(0)}`;
        document.getElementById("cart-total-amount").textContent = `₹${subtotal.toFixed(0)}`;
    },

    modifyCartQty(itemId, delta) {
        const itemIndex = this.state.cart.findIndex(i => i.id === itemId);
        if (itemIndex > -1) {
            this.state.cart[itemIndex].quantity += delta;
            if (this.state.cart[itemIndex].quantity <= 0) {
                this.state.cart.splice(itemIndex, 1);
            }
        }
        this.updateCartUI();
    },

    toggleCart() {
        const drawer = document.getElementById("cart-drawer");
        const overlay = document.getElementById("cart-drawer-overlay");
        drawer.classList.toggle("hidden");
        overlay.classList.toggle("hidden");
    },

    // ==========================================
    // 4. Payment & End-to-End Order Creation
    // ==========================================
    openPaymentModal() {
        if (this.state.cart.length === 0) {
            this.showToast("Your cart is empty!", "error");
            return;
        }

        const subtotal = this.state.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        document.getElementById("pay-total-display").textContent = `₹${subtotal.toFixed(0)}`;
        
        this.toggleCart();
        document.getElementById("payment-modal-overlay").classList.remove("hidden");
        document.getElementById("payment-kafka-progress").classList.add("hidden");
        document.getElementById("btn-submit-payment").classList.remove("hidden");
    },

    closePaymentModal() {
        document.getElementById("payment-modal-overlay").classList.add("hidden");
    },

    switchPaymentTab(method) {
        this.state.selectedPaymentMethod = method;
        document.getElementById("pay-tab-upi").classList.toggle("active", method === 'UPI');
        document.getElementById("pay-tab-card").classList.toggle("active", method === 'CARD');
        document.getElementById("pay-tab-cod").classList.toggle("active", method === 'COD');

        document.getElementById("pay-form-upi").classList.toggle("hidden", method !== 'UPI');
        document.getElementById("pay-form-card").classList.toggle("hidden", method !== 'CARD');
        document.getElementById("pay-form-cod").classList.toggle("hidden", method !== 'COD');
    },

    async processOrderAndPayment() {
        const subtotal = this.state.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const address = this.state.location ? this.state.location.address : "Taramani, Chennai 600113";
        const btn = document.getElementById("btn-submit-payment");
        const progressBox = document.getElementById("payment-kafka-progress");
        const stepText = document.getElementById("kafka-step-text");

        btn.classList.add("hidden");
        progressBox.classList.remove("hidden");

        // 1. Emitting Kafka Order Creation
        stepText.textContent = "1. Sending order to order-service (:8082)... Emitting Kafka 'order-created' event 🚀";

        const orderId = Math.floor(Math.random() * 9000) + 1000;
        const orderNumber = `ORD-CHN-${orderId}`;
        const cartItemsCopy = [...this.state.cart];

        try {
            const orderPayload = {
                customerId: 1,
                restaurantId: 1,
                deliveryAddress: address,
                contactPhone: "+91 98765 43210",
                specialInstructions: "Deliver near Ramanujan IT City gate 2, Taramani",
                items: cartItemsCopy.map(i => ({ menuItemId: i.id, quantity: i.quantity }))
            };

            await fetch("/api/orders", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(orderPayload)
            });
        } catch (err) {
            console.warn("Backend direct dispatch notice:", err);
        }

        // 2. Processing Payment
        await new Promise(r => setTimeout(r, 700));
        stepText.textContent = `2. Contacting payment-service (:8085)... Processing ₹${subtotal} via ${this.state.selectedPaymentMethod}... 💳`;

        const txnId = `TXN-${Math.floor(Math.random() * 900000) + 100000}`;

        try {
            const payPayload = {
                orderId: orderId,
                orderNumber: orderNumber,
                amount: subtotal,
                currency: "INR",
                paymentMethod: this.state.selectedPaymentMethod === 'CARD' ? 'CREDIT_CARD' : this.state.selectedPaymentMethod,
                cardNumber: "4111222233334444",
                cardExpiry: "12/28",
                cardCvv: "123",
                upiId: document.getElementById("pay-upi-id") ? document.getElementById("pay-upi-id").value : "customer@okhdfcbank"
            };

            await fetch("/api/payments/process", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payPayload)
            });
        } catch (err) {
            console.warn("Backend payment notice:", err);
        }

        // 3. Kafka Order-Paid Event
        await new Promise(r => setTimeout(r, 700));
        stepText.textContent = "3. Kafka event 'payment-processed' emitted! Order status: PAID. Kitchen notified! 👨‍🍳";

        await new Promise(r => setTimeout(r, 600));

        // Add to unified orders list
        const newOrder = {
            id: orderId,
            orderNumber: orderNumber,
            customerName: this.state.user.fullName || "Alex Customer",
            restaurantName: cartItemsCopy[0] ? cartItemsCopy[0].restaurant : "A2B - Adyar Ananda Bhavan (Taramani)",
            restaurantId: 1,
            deliveryAddress: address,
            items: cartItemsCopy.map(i => ({ name: i.name, quantity: i.quantity, price: i.price })),
            totalAmount: subtotal,
            paymentMethod: this.state.selectedPaymentMethod,
            paymentStatus: "PAID",
            status: "PENDING_RESTAURANT_ACCEPTANCE",
            placedAt: "Just now",
            courierName: null
        };

        this.state.orders.unshift(newOrder);

        // Complete & Show Success Modal
        this.closePaymentModal();
        this.state.cart = [];
        this.updateCartUI();
        this.renderCustomerRecentOrders();
        this.updateNotificationBadges();

        document.getElementById("success-order-num").textContent = orderNumber;
        document.getElementById("success-order-address").textContent = address;
        document.getElementById("success-txn-id").textContent = txnId;
        document.getElementById("order-success-modal").classList.remove("hidden");
    },

    closeSuccessModal() {
        document.getElementById("order-success-modal").classList.add("hidden");
        this.showToast("Order placed successfully! Track live status on this page.", "success");
    },

    viewOrderInKitchen() {
        this.closeSuccessModal();
        this.switchRole('restaurant');
    },

    renderCustomerRecentOrders() {
        const container = document.getElementById("customer-orders-list");
        if (!container) return;

        container.innerHTML = "";

        if (this.state.orders.length === 0) {
            container.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--text-muted);">
                    <p>No recent orders found. Place your first order from Chennai menus above!</p>
                </div>
            `;
            return;
        }

        this.state.orders.forEach(order => {
            const card = document.createElement("div");
            card.className = "customer-order-card";

            let statusBadge = `<span class="k-order-badge badge-pending">⏳ Waiting for Kitchen</span>`;
            if (order.status === 'PREPARING') statusBadge = `<span class="k-order-badge badge-preparing">🍳 Kitchen Preparing</span>`;
            if (order.status === 'READY_FOR_PICKUP') statusBadge = `<span class="k-order-badge badge-ready">📦 Ready for Courier</span>`;
            if (order.status === 'PICKED_UP' || order.status === 'OUT_FOR_DELIVERY') statusBadge = `<span class="k-order-badge badge-picked">🛵 Courier On the Way</span>`;
            if (order.status === 'DELIVERED') statusBadge = `<span class="k-order-badge badge-ready">✅ Delivered</span>`;
            if (order.status === 'REJECTED_BY_RESTAURANT') statusBadge = `<span class="k-order-badge badge-pending" style="color:var(--danger)">❌ Kitchen Rejected</span>`;

            const itemsSummary = order.items.map(i => `${i.quantity}x ${i.name}`).join(", ");

            card.innerHTML = `
                <div class="k-order-head">
                    <span class="k-order-id">${order.orderNumber}</span>
                    ${statusBadge}
                </div>
                <div class="k-order-customer">
                    <strong>${order.restaurantName}</strong>
                    <div style="margin-top: 0.3rem; font-size: 0.8rem; color: var(--text-muted);">${itemsSummary}</div>
                </div>
                <div class="k-order-footer">
                    <span class="k-order-total">₹${order.totalAmount}</span>
                    <small class="text-muted">📍 ${order.deliveryAddress.split(",")[0]}</small>
                </div>
            `;
            container.appendChild(card);
        });
    },

    // ==========================================
    // 5. Restaurant Kitchen Operations
    // ==========================================
    refreshRestaurantData() {
        this.renderKitchenOrders();
        this.renderKitchenMenuItems();
        this.updateKitchenMetrics();
        this.updateNotificationBadges();
    },

    updateKitchenMetrics() {
        const pending = this.state.orders.filter(o => o.status === 'PENDING_RESTAURANT_ACCEPTANCE').length;
        const preparing = this.state.orders.filter(o => o.status === 'PREPARING').length;
        const ready = this.state.orders.filter(o => o.status === 'READY_FOR_PICKUP').length;
        const activeDishes = this.state.catalog.filter(i => i.inStock).length;

        document.getElementById("kitchen-incoming-count").textContent = pending;
        document.getElementById("kitchen-preparing-count").textContent = preparing;
        document.getElementById("kitchen-ready-count").textContent = ready;
        document.getElementById("kitchen-menu-count").textContent = `${activeDishes}/${this.state.catalog.length}`;
    },

    renderKitchenOrders() {
        const container = document.getElementById("kitchen-orders-container");
        if (!container) return;

        container.innerHTML = "";

        if (this.state.orders.length === 0) {
            container.innerHTML = `<div style="text-align:center; padding: 2rem; color: var(--text-muted);">No orders in kitchen feed right now.</div>`;
            return;
        }

        this.state.orders.forEach(order => {
            const card = document.createElement("div");
            card.className = "kitchen-order-card";

            let badgeHtml = "";
            let actionButtons = "";

            if (order.status === 'PENDING_RESTAURANT_ACCEPTANCE') {
                badgeHtml = `<span class="k-order-badge badge-pending">Incoming Order 🔔</span>`;
                actionButtons = `
                    <button class="btn-success" onclick="App.kitchenAcceptOrder(${order.id})">✅ Accept Order</button>
                    <button class="btn-danger" onclick="App.kitchenRejectOrder(${order.id})">✕ Reject</button>
                `;
            } else if (order.status === 'PREPARING') {
                badgeHtml = `<span class="k-order-badge badge-preparing">🍳 Cooking in Kitchen</span>`;
                actionButtons = `
                    <button class="btn-primary" onclick="App.kitchenMarkReady(${order.id})">📦 Mark Ready for Courier</button>
                `;
            } else if (order.status === 'READY_FOR_PICKUP') {
                badgeHtml = `<span class="k-order-badge badge-ready">📦 Ready for Pickup</span>`;
                actionButtons = `<small class="text-success">Awaiting courier pickup in Taramani</small>`;
            } else if (order.status === 'PICKED_UP' || order.status === 'OUT_FOR_DELIVERY') {
                badgeHtml = `<span class="k-order-badge badge-picked">🛵 Picked Up by ${order.courierName || 'Murugan'}</span>`;
                actionButtons = `<small class="text-muted">Out for delivery</small>`;
            } else if (order.status === 'DELIVERED') {
                badgeHtml = `<span class="k-order-badge badge-ready">✅ Completed</span>`;
                actionButtons = `<small class="text-muted">Delivered to customer</small>`;
            } else {
                badgeHtml = `<span class="k-order-badge badge-pending" style="color:var(--danger)">Rejected</span>`;
                actionButtons = `<small class="text-danger">Order cancelled</small>`;
            }

            const itemsRows = order.items.map(item => `
                <div class="k-item-row">
                    <span>${item.quantity}x ${item.name}</span>
                    <strong style="color:var(--text-main)">₹${(item.price * item.quantity).toFixed(0)}</strong>
                </div>
            `).join("");

            card.innerHTML = `
                <div class="k-order-head">
                    <span class="k-order-id">${order.orderNumber} • ${order.customerName}</span>
                    ${badgeHtml}
                </div>
                <div class="k-order-customer">
                    <span>📍 Destination: <strong>${order.deliveryAddress}</strong></span>
                </div>
                <div class="k-order-items">
                    ${itemsRows}
                </div>
                <div class="k-order-footer">
                    <span class="k-order-total">Total: ₹${order.totalAmount}</span>
                    <div class="k-order-actions">
                        ${actionButtons}
                    </div>
                </div>
            `;
            container.appendChild(card);
        });
    },

    kitchenAcceptOrder(orderId) {
        const order = this.state.orders.find(o => o.id === orderId);
        if (order) {
            order.status = "PREPARING";
            this.refreshRestaurantData();
            this.showToast(`Accepted Order ${order.orderNumber}! Sent to chef preparation queue. 🍳`, "success");
        }
    },

    kitchenRejectOrder(orderId) {
        const order = this.state.orders.find(o => o.id === orderId);
        if (order) {
            order.status = "REJECTED_BY_RESTAURANT";
            this.refreshRestaurantData();
            this.showToast(`Rejected Order ${order.orderNumber}.`, "info");
        }
    },

    kitchenMarkReady(orderId) {
        const order = this.state.orders.find(o => o.id === orderId);
        if (order) {
            order.status = "READY_FOR_PICKUP";
            this.refreshRestaurantData();
            this.showToast(`Order ${order.orderNumber} is Packed! Dispatched alert to Chennai courier fleet. 📦`, "success");
        }
    },

    renderKitchenMenuItems() {
        const container = document.getElementById("kitchen-menu-items-container");
        if (!container) return;

        container.innerHTML = "";

        this.state.catalog.forEach(item => {
            const card = document.createElement("div");
            card.className = "kitchen-menu-card";
            card.innerHTML = `
                <div class="k-dish-info">
                    <div class="k-dish-emoji">${item.emoji}</div>
                    <div class="k-dish-text">
                        <h4>${item.name}</h4>
                        <span>₹${item.price} • ${item.category}</span>
                    </div>
                </div>
                <div class="k-dish-stock-toggle">
                    <span class="stock-status-pill ${item.inStock ? 'stock-in' : 'stock-out'}">
                        ${item.inStock ? 'IN STOCK' : 'OUT OF STOCK'}
                    </span>
                    <label class="switch">
                        <input type="checkbox" ${item.inStock ? 'checked' : ''} onchange="App.toggleDishStock(${item.id}, this.checked)">
                        <span class="slider round"></span>
                    </label>
                </div>
            `;
            container.appendChild(card);
        });
    },

    toggleDishStock(dishId, isStock) {
        const item = this.state.catalog.find(i => i.id === dishId);
        if (item) {
            item.inStock = isStock;
            this.renderKitchenMenuItems();
            this.updateKitchenMetrics();
            this.renderCatalog();
            const msg = isStock 
                ? `"${item.name}" is now IN STOCK & visible on Customer Menu!` 
                : `"${item.name}" is now marked OUT OF STOCK on Customer Menu.`;
            this.showToast(msg, isStock ? "success" : "info");
        }
    },

    openAddDishModal() {
        document.getElementById("add-dish-modal").classList.remove("hidden");
    },

    closeAddDishModal() {
        document.getElementById("add-dish-modal").classList.add("hidden");
    },

    handleAddDish(e) {
        e.preventDefault();
        const name = document.getElementById("new-dish-name").value.trim();
        const category = document.getElementById("new-dish-cat").value;
        const price = parseFloat(document.getElementById("new-dish-price").value) || 200;
        const emoji = document.getElementById("new-dish-emoji").value.trim() || "🍲";
        const description = document.getElementById("new-dish-desc").value.trim();

        const newDish = {
            id: Date.now(),
            name: name,
            category: category,
            cuisine: category,
            restaurant: "A2B - Adyar Ananda Bhavan (Taramani)",
            restaurantId: 1,
            price: price,
            rating: 5.0,
            emoji: emoji,
            inStock: true,
            description: description
        };

        this.state.catalog.unshift(newDish);
        this.closeAddDishModal();
        this.refreshRestaurantData();
        this.renderCatalog();
        this.showToast(`Added "${name}" to restaurant menu! 🍲`, "success");

        document.getElementById("new-dish-name").value = "";
        document.getElementById("new-dish-price").value = "";
        document.getElementById("new-dish-desc").value = "";
    },

    // ==========================================
    // 6. Delivery Partner Operations
    // ==========================================
    toggleDeliveryAvailability(isOnline) {
        this.state.deliveryOnline = isOnline;
        const label = document.getElementById("delivery-online-label");
        if (label) {
            label.innerHTML = `Status: <strong style="color:${isOnline ? 'var(--success)' : 'var(--text-muted)'}">${isOnline ? 'ONLINE' : 'OFFLINE'}</strong>`;
        }

        // Also update Murugan's status in the fleet table
        const murugan = this.state.couriers.find(c => c.id === 1);
        if (murugan) murugan.online = isOnline;

        this.showToast(`Delivery Partner status is now ${isOnline ? 'ONLINE (Ready for Orders)' : 'OFFLINE'}. 🛵`, isOnline ? "success" : "info");
        this.refreshDeliveryTasks();
    },

    refreshDeliveryTasks() {
        this.renderActiveDeliveryMission();
        this.renderAvailableDeliveryTasks();
        this.updateNotificationBadges();
    },

    renderActiveDeliveryMission() {
        const container = document.getElementById("delivery-active-mission-container");
        const badge = document.getElementById("active-task-badge");
        if (!container) return;

        const activeOrder = this.state.orders.find(o => (o.status === 'PICKED_UP' || o.status === 'OUT_FOR_DELIVERY') && (o.courierName === 'Murugan S.' || !o.courierName));

        if (!activeOrder) {
            badge.textContent = "No Active Task";
            badge.className = "badge-pill bg-dark";
            container.innerHTML = `
                <div style="text-align: center; padding: 2.5rem 1rem; color: var(--text-muted);">
                    <p style="font-size: 2.5rem; margin-bottom: 0.5rem;">🛵</p>
                    <p style="font-weight: 700; color: var(--text-main);">No delivery mission in progress.</p>
                    <small>Accept an available order from the queue below to start delivery!</small>
                </div>
            `;
            return;
        }

        badge.textContent = "Delivery In Progress";
        badge.className = "badge-pill bg-primary";

        container.innerHTML = `
            <div class="delivery-task-card">
                <div class="d-task-header">
                    <span class="d-order-num">${activeOrder.orderNumber} • ${activeOrder.customerName}</span>
                    <span class="k-order-badge badge-picked">OUT FOR DELIVERY</span>
                </div>
                <div class="d-route-timeline">
                    <div class="d-route-point">
                        <span>🏬</span>
                        <div>
                            <strong>Pickup: ${activeOrder.restaurantName}</strong>
                            <small>Items checked & picked up</small>
                        </div>
                    </div>
                    <div class="d-route-point">
                        <span>📍</span>
                        <div>
                            <strong>Drop: ${activeOrder.deliveryAddress}</strong>
                            <small>Customer awaiting delivery in Chennai</small>
                        </div>
                    </div>
                </div>
                <div class="d-task-footer">
                    <div>
                        <span class="d-payout-val">Payout: ₹80</span>
                        <small class="text-muted"> (${activeOrder.paymentMethod} Payment)</small>
                    </div>
                    <button class="btn-primary" onclick="App.deliveryMarkDelivered(${activeOrder.id})">
                        <span>✅ Confirm Order Delivered</span>
                    </button>
                </div>
            </div>
        `;
    },

    renderAvailableDeliveryTasks() {
        const container = document.getElementById("delivery-available-tasks-container");
        if (!container) return;

        container.innerHTML = "";

        if (!this.state.deliveryOnline) {
            container.innerHTML = `
                <div style="text-align: center; padding: 2.5rem 1rem; color: var(--text-muted);">
                    <p style="font-size: 2rem;">⏸️</p>
                    <p style="font-weight: 700;">You are currently OFFLINE.</p>
                    <small>Toggle your status to ONLINE above to view and accept incoming delivery orders.</small>
                </div>
            `;
            return;
        }

        const available = this.state.orders.filter(o => o.status === 'READY_FOR_PICKUP' || o.status === 'PREPARING');

        if (available.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 2.5rem 1rem; color: var(--text-muted);">
                    <p style="font-size: 2rem;">✅</p>
                    <p style="font-weight: 700;">All orders in Taramani Hub are dispatched!</p>
                    <small>New orders will appear here automatically when placed.</small>
                </div>
            `;
            return;
        }

        available.forEach(order => {
            const card = document.createElement("div");
            card.className = "delivery-task-card";
            card.innerHTML = `
                <div class="d-task-header">
                    <span class="d-order-num">${order.orderNumber}</span>
                    <span class="k-order-badge ${order.status === 'READY_FOR_PICKUP' ? 'badge-ready' : 'badge-preparing'}">
                        ${order.status === 'READY_FOR_PICKUP' ? 'READY AT KITCHEN' : 'BEING PREPARED'}
                    </span>
                </div>
                <div class="d-route-timeline">
                    <div class="d-route-point">
                        <span>🏬</span>
                        <div>
                            <strong>${order.restaurantName}</strong>
                            <small>Order Total: ₹${order.totalAmount} (${order.items.length} items)</small>
                        </div>
                    </div>
                    <div class="d-route-point">
                        <span>📍</span>
                        <div>
                            <strong>Destination: ${order.deliveryAddress}</strong>
                            <small>Distance: ~1.2 km from Taramani Hub</small>
                        </div>
                    </div>
                </div>
                <div class="d-task-footer">
                    <span class="d-payout-val">Partner Fee: ₹80</span>
                    <button class="btn-success" onclick="App.deliveryAcceptTask(${order.id})">
                        <span>🛵 Accept & Pick Up</span>
                    </button>
                </div>
            `;
            container.appendChild(card);
        });
    },

    deliveryAcceptTask(orderId) {
        const order = this.state.orders.find(o => o.id === orderId);
        if (order) {
            order.status = "OUT_FOR_DELIVERY";
            order.courierName = "Murugan S.";
            this.refreshDeliveryTasks();
            this.showToast(`Accepted Delivery for ${order.orderNumber}! Navigate to ${order.deliveryAddress.split(",")[0]}. 🛵`, "success");
        }
    },

    deliveryMarkDelivered(orderId) {
        const order = this.state.orders.find(o => o.id === orderId);
        if (order) {
            order.status = "DELIVERED";
            this.state.todayEarnings += 80;
            this.state.completedDeliveries += 1;

            document.getElementById("delivery-earnings-val").textContent = `₹${this.state.todayEarnings}`;
            document.getElementById("delivery-completed-val").textContent = this.state.completedDeliveries;

            this.refreshDeliveryTasks();
            this.showToast(`Order ${order.orderNumber} DELIVERED successfully! Earned ₹80. 🎉`, "success");
        }
    },

    // ==========================================
    // 7. Admin Console Operations
    // ==========================================
    refreshAdminData() {
        this.renderAdminKPIs();
        this.renderAdminOrdersTable();
        this.renderAdminFleetTable();
        this.showToast("Admin telemetry synchronized across 7 microservices! ⚡", "info");
    },

    renderAdminKPIs() {
        const totalOrders = this.state.orders.length;
        const totalGMV = this.state.orders.reduce((sum, o) => sum + (o.status !== 'REJECTED_BY_RESTAURANT' ? o.totalAmount : 0), 0);
        const onlineCouriers = this.state.couriers.filter(c => c.online).length;

        document.getElementById("admin-total-orders-kpi").textContent = totalOrders;
        document.getElementById("admin-gmv-kpi").textContent = `₹${totalGMV.toLocaleString('en-IN')}`;
        document.getElementById("admin-active-couriers-kpi").textContent = onlineCouriers;
        document.getElementById("admin-orders-count-badge").textContent = `${totalOrders} Orders`;
    },

    renderAdminOrdersTable() {
        const tbody = document.getElementById("admin-orders-table-body");
        if (!tbody) return;

        tbody.innerHTML = "";

        this.state.orders.forEach(order => {
            const tr = document.createElement("tr");

            let statusBadge = `<span class="k-order-badge badge-pending">${order.status}</span>`;
            if (order.status === 'PREPARING') statusBadge = `<span class="k-order-badge badge-preparing">PREPARING</span>`;
            if (order.status === 'READY_FOR_PICKUP') statusBadge = `<span class="k-order-badge badge-ready">READY_FOR_PICKUP</span>`;
            if (order.status === 'OUT_FOR_DELIVERY' || order.status === 'PICKED_UP') statusBadge = `<span class="k-order-badge badge-picked">OUT_FOR_DELIVERY</span>`;
            if (order.status === 'DELIVERED') statusBadge = `<span class="k-order-badge badge-ready">DELIVERED</span>`;

            tr.innerHTML = `
                <td><strong>${order.orderNumber}</strong></td>
                <td>${order.customerName}</td>
                <td>${order.restaurantName.split("(")[0]}</td>
                <td><small>${order.deliveryAddress.split(",")[0]}</small></td>
                <td><strong>₹${order.totalAmount}</strong></td>
                <td><span class="upi-chip">${order.paymentMethod}</span></td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn-sm-action" onclick="App.adminInspectOrder(${order.id})">🔍 Inspect</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    },

    adminInspectOrder(orderId) {
        const order = this.state.orders.find(o => o.id === orderId);
        if (order) {
            alert(
                `[ADMIN INSPECTOR]\nOrder: ${order.orderNumber}\nCustomer: ${order.customerName}\nAddress: ${order.deliveryAddress}\nTotal: ₹${order.totalAmount}\nStatus: ${order.status}\nCourier: ${order.courierName || 'Unassigned'}`
            );
        }
    },

    renderAdminFleetTable() {
        const tbody = document.getElementById("admin-fleet-table-body");
        if (!tbody) return;

        tbody.innerHTML = "";

        this.state.couriers.forEach(c => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>#COU-00${c.id}</td>
                <td><strong>${c.name}</strong></td>
                <td><small>${c.vehicle}</small></td>
                <td>${c.phone}</td>
                <td>
                    <span class="badge-pill ${c.online ? 'bg-success' : 'bg-dark'}">
                        ${c.online ? 'ONLINE' : 'OFFLINE'}
                    </span>
                </td>
                <td>📍 ${c.zone}</td>
                <td><strong>${c.rating}</strong></td>
            `;
            tbody.appendChild(tr);
        });
    },

    // ==========================================
    // 8. Toast Notifications
    // ==========================================
    showToast(message, type = "info") {
        const container = document.getElementById("toast-container");
        if (!container) return;

        const toast = document.createElement("div");
        toast.className = `toast ${type}`;
        
        let icon = "ℹ️";
        if (type === "success") icon = "✅";
        if (type === "error") icon = "❌";

        toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = "0";
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }
};

document.addEventListener("DOMContentLoaded", () => {
    App.init();
});
