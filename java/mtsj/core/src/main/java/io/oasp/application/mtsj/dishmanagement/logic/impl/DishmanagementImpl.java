package io.oasp.application.mtsj.dishmanagement.logic.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.oasp.application.mtsj.dishmanagement.dataaccess.api.DishEntity;
import io.oasp.application.mtsj.dishmanagement.dataaccess.api.dao.DishDao;
import io.oasp.application.mtsj.dishmanagement.logic.api.Dishmanagement;
import io.oasp.application.mtsj.dishmanagement.logic.api.to.DishEto;
import io.oasp.application.mtsj.dishmanagement.logic.api.to.DishSearchCriteriaTo;
import io.oasp.application.mtsj.general.logic.base.AbstractComponentFacade;
import io.oasp.application.mtsj.imagemanagement.dataaccess.api.ImageEntity;
import io.oasp.application.mtsj.imagemanagement.logic.api.Imagemanagement;
import io.oasp.module.jpa.common.api.to.PaginatedListTo;

/**
 * Implementation of component interface of dishmanagement
 */
@Named
public class DishmanagementImpl extends AbstractComponentFacade implements Dishmanagement {

  /**
   * Logger instance.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DishmanagementImpl.class);

  /**
   * @see #getDishDao()
   */
  @Inject
  private DishDao dishDao;

  @Inject
  private Imagemanagement imageManagement;

  /**
   * The constructor.
   */
  public DishmanagementImpl() {

    super();
  }

  @Override
  public DishEto findDish(Long id) {

    LOG.debug("Get Dish with id {} from database.", id);
    return getBeanMapper().map(getDishDao().findOne(id), DishEto.class);
  }

  @Override
  public PaginatedListTo<DishEto> findDishEtos(DishSearchCriteriaTo criteria) {

    criteria.limitMaximumPageSize(MAXIMUM_HIT_LIMIT);
    PaginatedListTo<DishEntity> dishs = getDishDao().findDishs(criteria);
    dishs = getImage(dishs);
    return mapPaginatedEntityList(dishs, DishEto.class);
  }

  @Override
  public boolean deleteDish(Long dishId) {

    DishEntity dish = getDishDao().find(dishId);
    getDishDao().delete(dish);
    LOG.debug("The dish with id '{}' has been deleted.", dishId);
    return true;
  }

  @Override
  public DishEto saveDish(DishEto dish) {

    Objects.requireNonNull(dish, "dish");
    DishEntity dishEntity = getBeanMapper().map(dish, DishEntity.class);

    // initialize, validate dishEntity here if necessary
    getDishDao().save(dishEntity);
    LOG.debug("Dish with id '{}' has been created.", dishEntity.getId());

    return getBeanMapper().map(dishEntity, DishEto.class);
  }

  /**
   * Returns the field 'dishDao'.
   *
   * @return the {@link DishDao} instance.
   */
  public DishDao getDishDao() {

    return this.dishDao;
  }

  /**
   * @param dishDao the {@link DishDao} to {@link Inject}.
   */
  @Inject
  public void setDishDao(DishDao dishDao) {

    this.dishDao = dishDao;
  }

  private PaginatedListTo<DishEntity> getImage(PaginatedListTo<DishEntity> entities) {

    List<DishEntity> dishes = entities.getResult();
    List<DishEntity> dishesImg = new ArrayList<>();
    for (DishEntity dish : dishes) {
      dish.setImage(getBeanMapper().map(this.imageManagement.findImage(dish.getIdImage()), ImageEntity.class));
      dishesImg.add(dish);
    }
    return new PaginatedListTo<>(dishesImg, entities.getPagination());
  }

}