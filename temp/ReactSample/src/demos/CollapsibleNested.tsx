/**
 * TEST G: Nested Collapsible groups
 * What happens when a collapsible Flex contains another collapsible Flex?
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';

export function CollapsibleNested() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Test G: Nested Collapsible</Typography>

      {/* G1: Simple nesting */}
      <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>G1: Collapsible inside collapsible</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h5">Outer Group</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <TextField fullWidth label="Outer Field 1" />

              {/* Nested collapsible */}
              <Accordion defaultExpanded>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography variant="h6">Inner Group A</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Stack spacing={2}>
                    <TextField fullWidth label="Inner Field A1" />
                    <TextField fullWidth label="Inner Field A2" />
                  </Stack>
                </AccordionDetails>
              </Accordion>

              <Accordion defaultExpanded>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography variant="h6">Inner Group B</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Stack spacing={2}>
                    <TextField fullWidth label="Inner Field B1" />
                    <TextField fullWidth label="Inner Field B2" />
                  </Stack>
                </AccordionDetails>
              </Accordion>
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* G2: Collapsible inside Card */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>G2: Collapsible child inside a Card parent</Typography>
      <Grid size={{ xs: 12 }}>
        <Card>
          <CardContent>
            <Grid container direction="row" alignItems="center" justifyContent="space-between" spacing={2} sx={{ mb: 2 }}>
              <Grid>
                <Typography variant="h5">Card Parent</Typography>
              </Grid>
            </Grid>
            <Stack spacing={2}>
              <TextField fullWidth label="Card Field 1" />
              <Accordion defaultExpanded>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography variant="h6">Collapsible Inside Card</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Stack spacing={2}>
                    <TextField fullWidth label="Nested Field 1" />
                    <TextField fullWidth label="Nested Field 2" />
                  </Stack>
                </AccordionDetails>
              </Accordion>
            </Stack>
          </CardContent>
        </Card>
      </Grid>

      {/* G3: Card inside Collapsible */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>G3: Card child inside a Collapsible parent</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h5">Collapsible Parent</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>Card Child</Typography>
                  <Stack spacing={2}>
                    <TextField fullWidth label="Card Field 1" />
                    <TextField fullWidth label="Card Field 2" />
                  </Stack>
                </CardContent>
              </Card>
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>
    </Box>
  );
}
