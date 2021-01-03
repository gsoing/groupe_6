package io.swagger.api;

import io.swagger.dto.DocumentDto;
import io.swagger.exception.*;
import io.swagger.model.*;
import io.swagger.service.DocumentService;
import io.swagger.service.LockService;
import io.swagger.utils.RestUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * Il existe des façons beaucoup plus propre que de déclencher des NPE pour faire vos actions,
 * ce n'est pas performant puisque vous en déclenchez une deuxième exception derrière et en plus elles sont facilement
 * évitables en inversant les tests, mavar != null
 * en plus cela rend le code difficilement lisible.
 * De plus le découpage n'est pas clair entre la couche controller et la couche service, on trouve du fonctionnel un peu partout
 * ca rend la lecture très compliquée
 */

@RestController
@AllArgsConstructor
@Slf4j
public class DocumentApiController {

    public DocumentService documentService;
    public LockService lockService;

    // GET A DOCUMENT
    @RequestMapping(value = "/documents/{documentId}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    public ResponseEntity<Document> documentsDocumentIdGet(@PathVariable("documentId") String documentId) {
        Document document = documentService.getDocument(documentId);

        // tout simplement pour éviter la NPE
        // if (document != null) {
        //    .....
        // } else {
        //  throw new NotFoundException("Any document with id " + documentId);
        // }
        try {
            document.equals(null);
        } catch (NullPointerException e){
            // Alors en anglais dans ce contexte any veut dire n'importe lequel, un simple 'no document with id ...' suffit
            throw new NotFoundException("Any document with id " + documentId);
        }
        // Dans la norme HTTP FOUND ne retourne pas de body mais fait une redirection vers une autre URL, une simple 200 aurait été suffisant
        return new ResponseEntity<Document>(document, HttpStatus.FOUND);
    }

    // PUT DOCUMENT
    @RequestMapping(value = "/documents/{documentId}", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.PUT)
    public ResponseEntity<DocumentDto> documentsDocumentIdPut(@PathVariable("documentId") String documentId, @RequestBody DocumentDto body) {

        // Tout cette partie d'algorithmie aurait du être faite dans le service

        Document updatedDocument = null;
        Lock lock = lockService.getLock(documentId);
        boolean doIt = false;

        try {
            lock.equals(null);
        } catch (NullPointerException e) {
            doIt = true;
        }

        // C'est dangereux de faire cela car normalement dans un OR java devrait tester les 2 paramètres
        // si doIt est à true alors lock est null et on aurait encore une NPE, ici coup de bol ca tombe en marche.
        if(doIt || lock.getOwner().equals(RestUtils.returnLoggedUser())) {
            if(body.getVersion() != documentService.getDocument(documentId).getVersion()) {
                throw new ConflictException("Version conflict");
            }

            try {
                updatedDocument = documentService.updateDocument(documentId, body.toEntity(), RestUtils.returnLoggedUser());
            } catch (NullPointerException f) {
                throw new NotFoundException("Document Not Found");
            }

            DocumentDto updatedDocumentDto = updatedDocument.toDto();
            return new ResponseEntity<DocumentDto>(updatedDocumentDto, HttpStatus.OK);
        } else {
            throw new LockedException("Document is locked");
        }
    }

    // PUT DOCUMENT STATUS
    @RequestMapping(value = "/documents/{documentId}/status", produces = { "application/json" }, consumes = { "text/plain" }, method = RequestMethod.PUT)
    // En déclarant le body en tant que Document.StatusEnum spring fait la conversion
    public ResponseEntity documentsDocumentIdStatusPut(@PathVariable("documentId") String documentId, @RequestBody String body) {

        // ou tout simplement vu que vous aimez utiliser les equals
        if(!Document.StatusEnum.VALIDATED.equals(Document.StatusEnum.fromValue(body))) {
            throw new BadRequestException("The status must be VALIDATED");
        }

        Document document;
        Document.StatusEnum bodyEnum = Document.StatusEnum.fromValue(body);
        try{
            document = documentService.updateStatus(documentId, bodyEnum);
        } catch (NullPointerException e) {
            throw new NotFoundException("Any document with id " + documentId);
        }

        // La logique métier devrait être dans le service c'est lui qui devrait lever l'exception
        // ici on ne comprend pas le lien entre un document null et l'exception
        if(document.equals(null)) {
            throw new ForbiddenException("This document is already validated");
        }

        // Plutot un 204 dans ce cas ici
        return new ResponseEntity(HttpStatus.OK);
    }

    // GET DOCUMENTS
    @RequestMapping(value = "/documents", produces = { "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<DocumentsList> documentsGet(@PageableDefault(page = 0, size = 20) Pageable pageable) {

        DocumentsList documents = documentService.getAll(pageable);
        // Pareil FOUND c'est une 302, donc une redirection sans body
        return new ResponseEntity<DocumentsList>(documents, HttpStatus.FOUND);
    }

    // POST DOCUMENT
    // La logique n'est pas correcte par rapport aux normes HTTP, si l'utilisateur passe l'id dans ce cas c'est un PUT sur
    // /documents/{id}, ici dans le cadre d'un POST c'est le système qui définit l'id
    // Pkoi retourné un résumé du document ???
    @RequestMapping(value = "/documents", produces = { "application/json" }, consumes = { "application/json" }, method = RequestMethod.POST)
    public ResponseEntity<DocumentSummary> documentsPost(@RequestBody DocumentDto body) {

        try {
            documentService.getDocument(body.getDocumentId()).equals(null);
        } catch (NullPointerException e) {
            DocumentSummary createdDocumentSummary = documentService.createDocument(body.toEntity(), RestUtils.returnLoggedUser());
            return new ResponseEntity<DocumentSummary>(createdDocumentSummary, HttpStatus.CREATED);
        }
        throw new ConflictException("Already exist");

    }

}
