package com.example.lab.carapplicationweb.services.impl;

import com.example.lab.carapplicationweb.models.Model;
import com.example.lab.carapplicationweb.repositories.BrandRepository;
import com.example.lab.carapplicationweb.repositories.ModelRepository;
import com.example.lab.carapplicationweb.services.ModelService;
import com.example.lab.carapplicationweb.services.dtos.AddModelDto;
import com.example.lab.carapplicationweb.services.dtos.ModelDTO;
import com.example.lab.carapplicationweb.services.dtos.ShowDetailedModelInfoDto;
import com.example.lab.carapplicationweb.services.dtos.ShowModelInfoDto;
import com.example.lab.carapplicationweb.util.ValidationUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@EnableCaching
public class ModelServiceImpl implements ModelService {
    private ModelRepository modelRepository;
    private BrandRepository brandRepository;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public ModelServiceImpl(ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Autowired
    public void setModelRepository (ModelRepository modelRepository){ this.modelRepository = modelRepository; }

    @Autowired
    public void setBrandRepository (BrandRepository brandRepository){ this.brandRepository = brandRepository; }

    @Override
    @CacheEvict(cacheNames = "models", allEntries = true)
    public void add(AddModelDto modelDTO) {
        if (!this.validationUtil.isValid(modelDTO))
        {
            this.validationUtil
                    .violations(modelDTO)
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .forEach(System.out::println);
        } else {
            try {
                Model model = modelMapper.map(modelDTO, Model.class);
                model.setImageURL("https://someimage.ru/firtsCarImage.jpg");
                model.setBrand(brandRepository.findByName(modelDTO.getBrandName()).orElse(null));
                this.modelRepository.saveAndFlush(model);
            } catch (Exception e) {
                System.out.println("Oops, something went wrong! :(");
            }
        }
    }

    @Override
//    @CacheEvict(cacheNames = "models", allEntries = true)
    public void update(String uuid, ModelDTO newModelDTO) {
        if (!this.validationUtil.isValid(newModelDTO))
        {
            this.validationUtil
                    .violations(newModelDTO)
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .forEach(System.out::println);
        } else {
            try {
                Optional<Model> existingBrand = modelRepository.findById(uuid);
                if (existingBrand == null)
                    throw new EntityNotFoundException("Model not found!");
                Model model = existingBrand.get();
                modelMapper.map(newModelDTO, model);
                modelRepository.saveAndFlush(model);
            } catch (Exception e) {
                System.out.println("Oops, something went wrong! :(");
            }
        }
    }

    @Override
    public ShowDetailedModelInfoDto modelDetails(String modelName)
    {
        return modelMapper.map(modelRepository.findByName(modelName).orElse(null), ShowDetailedModelInfoDto.class);
    }

    @Override
//    @CacheEvict(cacheNames = "models", allEntries = true)
    public void deleteByUuid(String uuid) { modelRepository.deleteById(uuid); }

    @Override
//    @CacheEvict(cacheNames = "models", allEntries = true)
    public void deleteByFullName(String fullName)
    {
        modelRepository.deleteModelByFullName(fullName);
    }

//    @Autowired
//    private EntityManager entityManager;
//
//    @Transactional
//    public void deleteByFullName(String fullName) {
//        Model model = modelRepository.findByFullName(fullName).orElse(null);
//        if (model != null) {
//            entityManager.remove(model);
//        }
//    }

    @Override
    public Optional<Model> findByUuid(String uuid){ return modelRepository.findById(uuid); }

    @Override
    @Cacheable("models")
    public List<ShowModelInfoDto> getAll() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return modelRepository.findAll().stream().map(model -> modelMapper.map(model, ShowModelInfoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(cacheNames = {"models", "offers"},  allEntries = true)
    public void deleteByName(String modelName) {
        modelRepository.deleteByName(modelName);
    }
}
