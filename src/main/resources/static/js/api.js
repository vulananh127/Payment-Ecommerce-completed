// ============================================================
// api.js - Đặt tại: src/main/resources/static/js/api.js
// ============================================================

const API_BASE = '/api/v1';

// ============================================================
// HELPERS
// ============================================================

function getToken() { return localStorage.getItem('accessToken'); }
function getUser() { return JSON.parse(localStorage.getItem('user') || 'null'); }
function isLoggedIn() { return !!getToken(); }

// Dùng ở đầu các trang cần đăng nhập (cart, checkout, orders)
function guardLogin() {
  if (!isLoggedIn()) {
    sessionStorage.setItem('redirectAfterLogin', window.location.href);
    window.location.href = '/pages/login.html';
    return false;
  }
  return true;
}

// Dùng ở đầu các trang admin
function guardAdmin() {
  if (!isLoggedIn()) {
    sessionStorage.setItem('redirectAfterLogin', window.location.href);
    window.location.href = '/pages/login.html';
    return false;
  }
  const user = getUser();
  if (!user || (user.role !== 'ADMIN' && user.role !== 'SHOP')) {
    alert('Bạn không có quyền truy cập trang này');
    window.location.href = '/index.html';
    return false;
  }
  return true;
}

function apiLogout() {
  ['accessToken', 'refreshToken', 'user', 'cart', 'voucher'].forEach(k => localStorage.removeItem(k));
  window.location.href = '/pages/login.html';
}

function authHeaders() {
  return { 'Content-Type': 'application/json', 'Authorization': `Bearer ${getToken()}` };
}

function publicHeaders() {
  return { 'Content-Type': 'application/json' };
}

async function handleResponse(res) {
  const body = await res.json();
  if (!res.ok || body.isSuccess === false) throw new Error(body.message || `Lỗi ${res.status}`);
  return body.data !== undefined ? body.data : body;
}

function formatPrice(amount) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
}

function formatDate(str) {
  if (!str) return '';
  return new Date(str).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

// ============================================================
// TOAST
// ============================================================

function showToast(msg, type = 'success', duration = 3000) {
  let t = document.getElementById('_toast');
  if (!t) {
    t = document.createElement('div');
    t.id = '_toast';
    t.style.cssText = 'position:fixed;bottom:2rem;right:2rem;padding:0.75rem 1.25rem;border-radius:8px;font-size:0.9rem;z-index:9999;display:none;font-family:Segoe UI,sans-serif;max-width:320px;';
    document.body.appendChild(t);
  }
  t.style.background = type === 'error' ? '#e53935' : '#111';
  t.style.color = '#fff';
  t.textContent = msg;
  t.style.display = 'block';
  clearTimeout(t._t);
  t._t = setTimeout(() => t.style.display = 'none', duration);
}

// ============================================================
// AUTH
// ============================================================

async function apiLogin(email, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST', headers: publicHeaders(),
    body: JSON.stringify({ email, password })
  });
  const body = await res.json();
  if (!res.ok || body.isSuccess === false) throw new Error(body.message || 'Email hoặc mật khẩu không đúng');
  const data = body.data || body;
 
  const token = data.accessToken || data.token || data.access_token;
  if (token) {
    localStorage.setItem('accessToken', token);
 
    // Decode JWT payload để lấy role, name, email
    // JWT có claim "user": { id, email, name, role }
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const userClaim = payload.user || {};
      const user = {
        id:    userClaim.id    || null,
        email: userClaim.email || email,
        name:  userClaim.name  || email,
        role:  userClaim.role  || 'USER'
      };
      localStorage.setItem('user', JSON.stringify(user));
    } catch (e) {
      localStorage.setItem('user', JSON.stringify({ email, role: 'USER' }));
    }
  }
 
  const refresh = data.refreshToken || data.refresh_token;
  if (refresh) localStorage.setItem('refreshToken', refresh);
 
  return data;
}

async function apiRegister(name, email, password) {
  const res = await fetch(`${API_BASE}/users/signup`, {
    method: 'POST', headers: publicHeaders(),
    body: JSON.stringify({ name, email, password })
  });
  return handleResponse(res);
}

// ============================================================
// CATEGORIES
// ============================================================

async function apiGetCategories() {
  const res = await fetch(`${API_BASE}/categories`, { headers: publicHeaders() });
  const data = await handleResponse(res);
  return Array.isArray(data) ? data : (data.content || data.categories || []);
}
 
async function apiCreateCategory(name) {
  const res = await fetch(`${API_BASE}/admin/categories`, {
    method: 'POST', headers: authHeaders(),
    body: JSON.stringify({ categoryName: name })
  });
  return handleResponse(res);
}
 
async function apiUpdateCategory(id, name) {
  const res = await fetch(`${API_BASE}/admin/categories/${id}`, {
    method: 'PUT', headers: authHeaders(),
    body: JSON.stringify({ categoryName: name })
  });
  return handleResponse(res);
}
 
async function apiDeleteCategory(id) {
  const res = await fetch(`${API_BASE}/admin/categories/${id}`, {
    method: 'DELETE', headers: authHeaders()
  });
  if (!res.ok) { const b = await res.json().catch(() => ({})); throw new Error(b.message || 'Không thể xóa'); }
  return true;
}
// ============================================================
// PRODUCTS
// ============================================================

async function apiGetProducts(params = {}) {
  const q = new URLSearchParams(params).toString();
  const res = await fetch(`${API_BASE}/products${q ? '?' + q : ''}`, { headers: publicHeaders() });
  const data = await handleResponse(res);
  return Array.isArray(data) ? data : (data.content || data.products || []);
}

async function apiGetProduct(id) {
  const res = await fetch(`${API_BASE}/products/${id}`, { headers: publicHeaders() });
  return handleResponse(res);
}

async function apiCreateProduct(data) {
    const res = await fetch('/api/v1/admin/products', {
      method: 'POST', headers: authHeaders(), body: JSON.stringify(data)
    });
    return handleResponse(res);
  }

  async function apiUpdateProduct(id, data) {
    const res = await fetch(`/api/v1/admin/products/${id}`, {
      method: 'PUT', headers: authHeaders(), body: JSON.stringify(data)
    });
    return handleResponse(res);
  }

  async function apiDeleteProduct(id) {
    const res = await fetch(`/api/v1/admin/products/${id}`, {
      method: 'DELETE', headers: authHeaders()
    });
    if (!res.ok) { const b = await res.json().catch(()=>({})); throw new Error(b.message||'Không thể xóa'); }
    return true;
  }

// ============================================================
// ORDERS
// ============================================================

async function apiCreateOrder(data) {
  const res = await fetch(`${API_BASE}/orders`, { method: 'POST', headers: authHeaders(), body: JSON.stringify(data) });
  return handleResponse(res);
}

async function apiGetMyOrders() {
  const res = await fetch(`${API_BASE}/orders`, { headers: authHeaders() });
  const data = await handleResponse(res);
  return Array.isArray(data) ? data : (data.content || data.orders || []);
}

// Thiếu xem chi tiết đơn hàng

async function apiCancelOrder(id) {
  const res = await fetch(`${API_BASE}/orders/${id}/cancel`, { method: 'PUT', headers: authHeaders() });
  return handleResponse(res);
}

async function apiGetAllOrders(params = {}) {
  const q = new URLSearchParams(params).toString();
  const res = await fetch(`${API_BASE}/admin/orders`, { headers: authHeaders() });
  const data = await handleResponse(res);
  return Array.isArray(data) ? data : (data.content || data.orders || []);
}

async function apiUpdateOrderStatus(id, status) {
  const res = await fetch(`${API_BASE}/admin/orders/${id}/status`, {
    method: 'PUT', headers: authHeaders(), body: JSON.stringify({ status })
  });
  return handleResponse(res);
}

// ============================================================
// PAYMENTS
// ============================================================

async function apiCreatePayment(data) {
  const res = await fetch(`${API_BASE}/payments`, { method: 'POST', headers: authHeaders(), body: JSON.stringify(data) });
  return handleResponse(res);
}

// ============================================================
// CART - localStorage
// ============================================================

function getCart() { return JSON.parse(localStorage.getItem('cart') || '[]'); }

function saveCart(cart) {
  localStorage.setItem('cart', JSON.stringify(cart));
  // Cập nhật badge nếu có
  const b = document.getElementById('cartBadge');
  if (b) b.textContent = cart.length;
}

function addToCart(product, qty = 1, variantId = null) {

  if (!isLoggedIn()) {
    showToast('Vui lòng đăng nhập để thêm vào giỏ hàng', 'error');

    setTimeout(() => {
      sessionStorage.setItem(
        'redirectAfterLogin',
        window.location.href
      );
      window.location.href =
        '/pages/login.html';
    }, 1500);
    return false;
  }
  const cart = getCart();
  let price = Number(product.price)|| 0;
  let image = product.imageUrl || "";
  let name = product.name;
  // ✅ nếu có variant
  if (variantId && product.variants) {

    const v =
      product.variants.find(
        x => x.id === variantId
      );

    if (v) {
      price = Number(v.price);
    }

  }
  const key = variantId || product.id;
  const existing =
    cart.find(
      i => (i.variantId || i.id) === key
    );
  if (existing) {
    existing.qty += qty;
  } else {
    cart.push({
      id: product.id,
      variantId: variantId,
      name: name,
      price: price,
      imageUrl: image,
      qty: qty
    });
  }
  saveCart(cart);
  showToast(
    `✓ Đã thêm "${product.name}" vào giỏ hàng`
  );

  return true;
}

function removeFromCart(id) { saveCart(getCart().filter(i => i.id !== id)); }

function updateCartQty(id, qty) {
  const cart = getCart();
  const item = cart.find(i => i.id === id);
  if (!item) return;
  if (qty <= 0) removeFromCart(id);
  else { item.qty = qty; saveCart(cart); }
}

function clearCart() {
  localStorage.removeItem('cart');
  const b = document.getElementById('cartBadge');
  if (b) b.textContent = 0;
}

function getCartTotal() { return getCart().reduce((s, i) => {

    const price = Number(i.price);
    const qty = Number(i.qty);

    return s + price * qty;

  }, 0);
}

function getCartCount() { return getCart().length; }

function buildOrderBody(formData, paymentMethod) {
  const voucher = JSON.parse(localStorage.getItem('voucher') || 'null');
  return {
    receiverName: formData.receiverName,
    phone: formData.phone,
    email: formData.email,
    note: formData.note || '',
    paymentMethod: paymentMethod,
    shippingFee: 0,
    totalAmount: getCartTotal(),
    address: {
      province: formData.province,
      district: formData.district,
      commune: formData.commune,
      address: formData.address
    },
    productVariants: getCart().map(i => ({
      variantId: i.variantId || i.id,
      quantity: i.qty
    })),
    voucherIds: voucher ? [voucher.id] : []
  };
}

// ============================================================
// INIT - Chỉ cập nhật cart badge, KHÔNG tự gắn sự kiện logout
// (Mỗi trang tự xử lý nav của mình)
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
  const b = document.getElementById('cartBadge');
  if (b) b.textContent = getCartCount();
});
