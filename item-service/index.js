const express = require('express');
const app = express();
app.use(express.json());

// In-memory storage
let items = [];
let idCounter = 1;

// Health check
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'UP' });
});

// GET /items
app.get('/items', (req, res) => {
  res.json(items);
});

// POST /items
app.post('/items', (req, res) => {
  const { name } = req.body;
  if (!name) {
    return res.status(400).json({ error: 'Name is required' });
  }
  const newItem = { id: idCounter++, name };
  items.push(newItem);
  res.status(201).json(newItem);
});

// GET /items/:id
app.get('/items/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const item = items.find(i => i.id === id);
  if (!item) {
    return res.status(404).json({ error: 'Item not found' });
  }
  res.json(item);
});

const PORT = process.env.PORT || 8081;
app.listen(PORT, () => console.log(`Item service running on port ${PORT}`));
