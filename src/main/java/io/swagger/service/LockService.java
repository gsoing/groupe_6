package io.swagger.service;

import io.swagger.model.Document;
import io.swagger.model.Lock;
import io.swagger.repository.LockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    private final LockRepository lockRepository;

    public Lock getLock(String documentId){
        Lock lockFound = lockRepository.findLockByDocumentId(documentId);
        return lockFound;
    }

    public Boolean deleteLockOnDocument(String documentId, String user){
        Lock lockFound = getLock(documentId);
        if(lockFound.getOwner().equals(user)) {
            lockRepository.deleteLockByDocumentId(documentId);
            if(getLock(documentId).equals(null)) return true;
        }

        return false;
    }

}