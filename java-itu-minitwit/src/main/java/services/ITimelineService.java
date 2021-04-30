package services;

import model.dto.MessagesPerUserDTO;
import model.dto.PublicTimelineDTO;
import model.dto.TimelineDTO;

public interface ITimelineService {
    Object publicTimeline(PublicTimelineDTO dto);
    Object timeline(TimelineDTO dto);
    Object userTimeline(MessagesPerUserDTO dto);
}
