package ok.admin.portlet.exception.model;

import java.util.List;
import java.util.stream.Collectors;

public record PortletByPosition(int position, double emptyProbability, List<Candidate> candidates) {

    @Override
    public String toString() {
        return """
                {
                   position=%d
                   emptyProbability=%f
                   candidates={
                        %s
                   }
                }
                """.formatted(position, emptyProbability, candidates.stream()
                .map(Candidate::toString)
                .collect(Collectors.joining(",\n")));
    }
}