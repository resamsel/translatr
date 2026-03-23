package com.translatr.repository;

import com.translatr.model.LinkedAccount;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LinkedAccountRepository implements PanacheRepositoryBase<LinkedAccount, Long> {
}
