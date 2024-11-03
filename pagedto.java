package DTO;

import java.util.List;

public class PageDTO<T> {
    private List<T> items; // The items on the current page
    private int currentPage; // Current page number
    private int totalItems; // Total number of items in all pages
    private int totalPages; // Total number of pages
    private int itemsPerPage; // Number of items per page

    // Constructor
    public PageDTO(List<T> items, int currentPage, int totalItems, int itemsPerPage) {
        this.items = items;
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.itemsPerPage = itemsPerPage;
        this.totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
    }

    // Getters and setters
    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }
}
