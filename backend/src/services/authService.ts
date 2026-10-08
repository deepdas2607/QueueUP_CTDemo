import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import { config } from '../config/env';
import { Role } from '../types';

const prisma = new PrismaClient();

export class AuthService {
  static async register(data: {
    name: string;
    email: string;
    password: string;
    occupation?: string;
    interests?: string;
  }) {
    const normalizedEmail = data.email.trim().toLowerCase();

    const existing = await prisma.user.findUnique({
      where: { email: normalizedEmail },
    });

    if (existing) {
      throw { status: 400, message: 'User with this email already exists' };
    }

    const passwordHash = await bcrypt.hash(data.password, 10);

    const user = await prisma.user.create({
      data: {
        name: data.name,
        email: normalizedEmail,
        passwordHash,
        occupation: data.occupation || null,
        interests: data.interests || null,
      },
    });

    const token = jwt.sign(
      { userId: user.id, email: user.email, role: user.role as Role },
      config.jwtSecret,
      { expiresIn: '30d' }
    );

    const { passwordHash: _, ...safeUser } = user;
    return { token, user: safeUser };
  }

  static async login(email: string, password: string) {
    const normalizedEmail = email.trim().toLowerCase();

    const user = await prisma.user.findUnique({
      where: { email: normalizedEmail },
    });

    if (!user) {
      throw { status: 401, message: 'Invalid email or password' };
    }

    const isValid = await bcrypt.compare(password, user.passwordHash);
    if (!isValid) {
      throw { status: 401, message: 'Invalid email or password' };
    }

    await prisma.user.update({
      where: { id: user.id },
      data: { lastActiveAt: new Date() },
    });

    const token = jwt.sign(
      { userId: user.id, email: user.email, role: user.role as Role },
      config.jwtSecret,
      { expiresIn: '30d' }
    );

    const { passwordHash: _, ...safeUser } = user;
    return { token, user: safeUser };
  }

  static async getUserProfile(userId: string) {
    const user = await prisma.user.findUnique({
      where: { id: userId },
    });

    if (!user) {
      throw { status: 404, message: 'User not found' };
    }

    const { passwordHash: _, ...safeUser } = user;
    return safeUser;
  }
}
