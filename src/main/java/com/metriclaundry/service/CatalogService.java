package com.metriclaundry.service;

import com.metriclaundry.dao.ServiceDAO;
import com.metriclaundry.model.LaundryService;
import com.metriclaundry.util.AppException;

import java.math.BigDecimal;
import java.util.List;

/** Manage Services (price list). */
public class CatalogService {
    private final ServiceDAO dao = new ServiceDAO();

    public List<LaundryService> list(boolean activeOnly) { return dao.findAll(activeOnly); }

    public void save(LaundryService s) {
        if (s.getName() == null || s.getName().isBlank()) throw new AppException("Service name is required.");
        if (s.getPrice() == null || s.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new AppException("Price must be greater than zero.");
        if (s.getTurnaroundHours() < 1) throw new AppException("Turnaround must be at least 1 hour.");
        s.setName(s.getName().trim());
        dao.save(s);
    }

    /** @return true if deleted, false if it was only deactivated because orders use it. */
    public boolean delete(int id) { return dao.deleteOrDeactivate(id); }
}
