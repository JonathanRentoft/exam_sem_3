package app.dao;

import java.util.List;

public interface IDao<T> {
    T create(T entity);
    T getById(int id);
    List<T> getAll();
    T update(T entity);
    void delete(int id);
}
