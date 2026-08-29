package com.translatr.service;

import com.translatr.criteria.MessageCriteria;
import com.translatr.dto.MessageDto;
import com.translatr.event.WordCountEventProducer;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Key;
import com.translatr.model.Locale;
import com.translatr.model.Message;
import com.translatr.model.Project;
import com.translatr.repository.KeyRepository;
import com.translatr.repository.LocaleRepository;
import com.translatr.repository.MessageRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock MessageRepository messageRepo;
    @Mock LocaleRepository  localeRepo;
    @Mock KeyRepository     keyRepo;
    @Mock DtoMapper         mapper;
    @Mock ActivityLogger    activity;
    @Mock WordCountEventProducer wordCounts;

    @InjectMocks MessageService service;

    @SuppressWarnings("unchecked")
    private PanacheQuery<Message> stubEmptyQuery() {
        PanacheQuery<Message> query = mock(PanacheQuery.class);
        when(messageRepo.find(anyString(), any(Object[].class))).thenReturn(query);
        when(query.count()).thenReturn(0L);
        when(query.page(anyInt(), anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of());
        return query;
    }

    private String capturedQuery() {
        ArgumentCaptor<String> ql = ArgumentCaptor.forClass(String.class);
        verify(messageRepo).find(ql.capture(), any(Object[].class));
        return ql.getValue();
    }

    @Test
    void find_translatesKeyNameIntoKeyNameFilter() {
        stubEmptyQuery();

        MessageCriteria c = new MessageCriteria();
        c.projectId = UUID.randomUUID();
        c.keyName   = "switch";
        c.limit     = 20;

        service.find(c);

        assertThat(capturedQuery()).contains("key.name = ");
    }

    @Test
    void find_translatesLocaleIdsCsvIntoInClause() {
        stubEmptyQuery();

        MessageCriteria c = new MessageCriteria();
        c.projectId = UUID.randomUUID();
        c.localeIds = UUID.randomUUID() + "," + UUID.randomUUID();
        c.limit     = 20;

        service.find(c);

        assertThat(capturedQuery()).contains("locale.id IN ");
    }

    @Test
    void get_throwsNotFound_whenMessageMissing() {
        UUID id = UUID.randomUUID();
        when(messageRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_persistsMessage() {
        UUID localeId = UUID.randomUUID();
        UUID keyId    = UUID.randomUUID();

        Locale locale = new Locale(); locale.id = localeId;
        Key key       = new Key();   key.id    = keyId;

        MessageDto dto = new MessageDto();
        dto.localeId   = localeId;
        dto.keyId      = keyId;
        dto.value      = "Hello";

        when(localeRepo.findByIdOptional(localeId)).thenReturn(Optional.of(locale));
        when(keyRepo.findByIdOptional(keyId)).thenReturn(Optional.of(key));
        when(mapper.toDto(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            MessageDto result = new MessageDto();
            result.value = m.value;
            return result;
        });

        MessageDto result = service.create(dto);

        verify(messageRepo).persist(any(Message.class));
        assertThat(result.value).isEqualTo("Hello");
    }

    @Test
    void create_publishesCreateActivity_forTheKeysProject() {
        UUID localeId = UUID.randomUUID();
        UUID keyId    = UUID.randomUUID();

        Project project = new Project("proj");
        Locale locale = new Locale(); locale.id = localeId;
        Key key       = new Key();   key.id    = keyId; key.project = project;

        MessageDto dto = new MessageDto();
        dto.localeId = localeId;
        dto.keyId    = keyId;
        dto.value    = "Hello";

        MessageDto after = new MessageDto();
        when(localeRepo.findByIdOptional(localeId)).thenReturn(Optional.of(locale));
        when(keyRepo.findByIdOptional(keyId)).thenReturn(Optional.of(key));
        when(mapper.toDto(any(Message.class))).thenReturn(after);

        service.create(dto);

        verify(activity).publish(eq(ActionType.Create), eq(project), eq(MessageDto.class),
                isNull(), eq(after));
    }

    @Test
    void update_publishesUpdateActivity_withBeforeAndAfter() {
        UUID id = UUID.randomUUID();
        Project project = new Project("proj");
        Key key = new Key(); key.project = project;
        Message msg = new Message();
        msg.id  = id;
        msg.key = key;

        MessageDto dto = new MessageDto();
        dto.id    = id;
        dto.value = "new value";

        MessageDto before = new MessageDto();
        MessageDto after  = new MessageDto();
        when(messageRepo.findByIdOptional(id)).thenReturn(Optional.of(msg));
        when(mapper.toDto(msg)).thenReturn(before, after);

        service.update(dto);

        verify(activity).publish(eq(ActionType.Update), eq(project), eq(MessageDto.class),
                eq(before), eq(after));
    }

    @Test
    void delete_publishesDeleteActivity_withBeforeSnapshot() {
        UUID id = UUID.randomUUID();
        Project project = new Project("proj");
        Key key = new Key(); key.project = project;
        Message msg = new Message();
        msg.id  = id;
        msg.key = key;

        MessageDto before = new MessageDto();
        when(messageRepo.findByIdOptional(id)).thenReturn(Optional.of(msg));
        when(mapper.toDto(msg)).thenReturn(before);

        service.delete(id);

        verify(activity).publish(eq(ActionType.Delete), eq(project), eq(MessageDto.class),
                eq(before), isNull());
    }

    @Test
    void create_throwsNotFound_whenLocaleMissing() {
        UUID localeId = UUID.randomUUID();
        MessageDto dto = new MessageDto();
        dto.localeId   = localeId;
        dto.keyId      = UUID.randomUUID();

        when(localeRepo.findByIdOptional(localeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_appliesValue() {
        UUID id      = UUID.randomUUID();
        Message msg  = new Message();
        msg.id       = id;
        msg.value    = "old";

        MessageDto dto = new MessageDto();
        dto.id    = id;
        dto.value = "new value";

        when(messageRepo.findByIdOptional(id)).thenReturn(Optional.of(msg));
        when(mapper.toDto(msg)).thenReturn(dto);

        service.update(dto);

        assertThat(msg.value).isEqualTo("new value");
    }

    @Test
    void update_throwsNotFound_whenMessageMissing() {
        UUID id = UUID.randomUUID();
        MessageDto dto = new MessageDto();
        dto.id = id;

        when(messageRepo.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_removesMessage() {
        UUID id     = UUID.randomUUID();
        Message msg = new Message();
        msg.id      = id;

        when(messageRepo.findByIdOptional(id)).thenReturn(Optional.of(msg));
        when(mapper.toDto(msg)).thenReturn(new MessageDto());

        service.delete(id);

        verify(messageRepo).delete(msg);
    }
}

