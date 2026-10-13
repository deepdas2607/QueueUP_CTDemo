// Handles fetching all campus service counters, single service details, and helpful campus tips.

import { Request, Response } from 'express';
import {
  getAllCampusServices,
  getCampusServiceById,
} from '../database/postgresDatabase';

// Returns the list of all 12 campus counters with their live waiting counts.
export async function getAllServices(req: Request, res: Response): Promise<void> {
  try {
    const services = await getAllCampusServices();
    res.json(services);
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to fetch services' });
  }
}

// Returns details for one specific campus counter when a user taps on it.
export async function getServiceById(req: Request, res: Response): Promise<void> {
  try {
    const service = await getCampusServiceById(String(req.params.id));
    if (!service) {
      res.status(404).json({ error: 'Service counter not found' });
      return;
    }
    res.json(service);
  } catch (error: any) {
    res.status(500).json({ error: error.message || 'Failed to fetch service detail' });
  }
}

// Returns helpful campus queue tips displayed on the home screen banner.
export async function getCampusTips(req: Request, res: Response): Promise<void> {
  res.json({
    tips: [
      'Arrive at the counter 2 minutes before your turn is called.',
      'Library and Bookstore counters have the shortest wait times before 11 AM.',
      'Keep your digital student ID ready for faster service verification.',
    ],
  });
}
