/**
 * TEST F: Collapsible + Horizontal direction
 * flex.hbs switches between Grid (horizontal) and Stack (vertical) for children
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

export function CollapsibleHorizontal() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Test F: Collapsible + Horizontal Layout</Typography>

      {/* F1: Collapsible with horizontal children (Grid) */}
      <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>F1: Horizontal children (isDirectionHorizontal=true)</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h5">Horizontal Group</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container alignItems="center" justifyContent="flex-start" spacing={2}>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField fullWidth label="Field A" />
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField fullWidth label="Field B" />
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField fullWidth label="Field C" />
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* F2: Collapsible with vertical children (Stack) */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>F2: Vertical children (isDirectionHorizontal=false)</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h5">Vertical Group</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2} className="force-full-width">
              <TextField fullWidth label="Field A" />
              <TextField fullWidth label="Field B" />
              <TextField fullWidth label="Field C" />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* F3: Multiple collapsible groups side by side */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>F3: Multiple collapsible groups side by side</Typography>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h6">Left Group</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Stack spacing={2}>
                <TextField fullWidth label="Left Field 1" />
                <TextField fullWidth label="Left Field 2" />
              </Stack>
            </AccordionDetails>
          </Accordion>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h6">Right Group</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Stack spacing={2}>
                <TextField fullWidth label="Right Field 1" />
                <TextField fullWidth label="Right Field 2" />
              </Stack>
            </AccordionDetails>
          </Accordion>
        </Grid>
      </Grid>
    </Box>
  );
}
