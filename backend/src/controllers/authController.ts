// Handles user registration, user login, and fetching the logged-in user's details.

import { Request, Response } from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import {
  findUserByEmail,
  findUserById,
  createNewUser,
} from '../database/postgresDatabase';
import { AuthRequest, JWT_SECRET } from '../middleware/authMiddleware';

// Registers a new user account, hashes their password, and returns a login token.
export async function registerUser(req: Request, res: Response): Promise<void> {
  try {
    const { name, email, password, phone, occupation, interests } = req.body;
    if (!name || !email || !password) {
      res.status(400).json({ error: 'Name, email, and password are required' });
      return;
    }

    const existingUser = await findUserByEmail(email);
    if (existingUser) {
      res.status(409).json({ error: 'An account with this email already exists' });
      return;
    }

    const passwordHash = await bcrypt.hash(password, 8);
    const newUser = await createNewUser({
      name,
      email,
      passwordHash,
      phone,
      occupation,
      interests,
    });

    const token = jwt.sign({ userId: newUser!.id }, JWT_SECRET, { expiresIn: '30d' });
    res.status(201).json({ token, user: newUser });
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to register user' });
  }
}

// Checks email and password against PostgreSQL and returns a JWT login token.
export async function loginUser(req: Request, res: Response): Promise<void> {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      res.status(400).json({ error: 'Email and password are required' });
      return;
    }

    const userRow = await findUserByEmail(email);
    if (!userRow) {
      res.status(401).json({ error: 'Invalid email or password' });
      return;
    }

    const isPasswordValid = await bcrypt.compare(password, userRow.password_hash);
    if (!isPasswordValid) {
      res.status(401).json({ error: 'Invalid email or password' });
      return;
    }

    const user = await findUserById(userRow.id);
    const token = jwt.sign({ userId: userRow.id }, JWT_SECRET, { expiresIn: '30d' });
    res.json({ token, user });
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to log in' });
  }
}

// Returns the currently logged-in user's profile based on their JWT token.
export async function getCurrentUser(req: AuthRequest, res: Response): Promise<void> {
  try {
    const user = await findUserById(req.userId!);
    if (!user) {
      res.status(404).json({ error: 'User not found' });
      return;
    }
    res.json(user);
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to load current user' });
  }
}
