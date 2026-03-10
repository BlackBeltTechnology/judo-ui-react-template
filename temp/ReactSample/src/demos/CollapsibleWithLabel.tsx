/**
 * TEST C: Collapsible + Label + Icon
 * Label and icon should appear in the AccordionSummary
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import PersonIcon from '@mui/icons-material/Person';
import { SampleChildrenVertical } from './SampleChildren';

export function CollapsibleWithLabel() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Test C: Collapsible + Label + Icon</Typography>

      {/* C1: Label only */}
      <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>C1: Label in AccordionSummary</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h5" component="h1">Personal Information</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* C2: Icon + Label */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>C2: Icon + Label in AccordionSummary</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Grid container direction="row" alignItems="center" justifyContent="flex-start">
              <PersonIcon sx={{ marginRight: 1 }} />
              <Typography variant="h5" component="h1">Personal Information</Typography>
            </Grid>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* C3: With h6 instead of h5 for smaller header */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>C3: Smaller header variant (h6)</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Grid container direction="row" alignItems="center" justifyContent="flex-start">
              <PersonIcon sx={{ marginRight: 1 }} />
              <Typography variant="h6" component="h2">Personal Information</Typography>
            </Grid>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>
    </Box>
  );
}
